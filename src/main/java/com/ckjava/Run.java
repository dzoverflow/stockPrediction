package com.ckjava;

import com.ckjava.bean.BuyReportBean;
import com.ckjava.bean.LastNUpBean;
import com.ckjava.bean.StockCodeBean;
import com.ckjava.service.*;
import com.ckjava.thread.runner.AnalysisRunner;
import com.ckjava.thread.runner.PredictionRunner;
import com.ckjava.xutils.*;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Future;

public class Run extends FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(Run.class);

    public static void main(String[] args) {
        ApplicationContext appc = new AnnotationConfigApplicationContext("com.ckjava");
        DownloadFileService downloadFileService = appc.getBean(DownloadFileService.class);
        StockPredictionService stockPredictionService = appc.getBean(StockPredictionService.class);
        BlacklistService blacklistService = appc.getBean(BlacklistService.class);
        FileService fileService = appc.getBean(FileService.class);
        ThreadService threadService = appc.getBean(ThreadService.class);
        AnalysisStockService analysisStockService = appc.getBean(AnalysisStockService.class);

        String downloadDateString = downloadFileService.getDownloadDateString();
        if (StringUtils.isBlank(downloadDateString)) {
            downloadDateString = DateUtils.formatTime(new Date().getTime(), "yyyyMMdd");
        }

        // 读取股票编码
        List<StockCodeBean> dataList = downloadFileService.getStockCodeList();

        // 下载股票数据文件
        if (downloadFileService.getIsDownloadRawFile()) {
            for (int i = 0, c = CollectionUtils.getSize(dataList); i < c; i++) {
                StockCodeBean stockCodeBean = dataList.get(i);
                if (blacklistService.isInBlacklist(stockCodeBean)) { // 如果在黑名单中就不再下载了
                    continue;
                }

                downloadFileService.downloadStockDataFile(downloadDateString, stockCodeBean);
                try {
                    long sleepTime = RandomUtils.nextLong(100, 2000);
                    Thread.currentThread().sleep(sleepTime);
                } catch (Exception e) {
                    logger.error(Run.class.getName().concat(".main has error"), e);
                }

            }
        } else {
            logger.info("取消下载股票数据文件");
        }

        String dataDateString = stockPredictionService.getDataDateString();
        if (StringUtils.isBlank(dataDateString)) {
            dataDateString = DateUtils.formatTime(new Date().getTime(), "yyyyMMdd");
        }

        String neuralDateString = stockPredictionService.getNeuralDateString();
        if (StringUtils.isBlank(neuralDateString)) {
            neuralDateString = DateUtils.formatTime(new Date().getTime(), "yyyyMMdd");
        }

        // 机器学习预测
        if (stockPredictionService.getIsPrediction()) {
            List<PredictionRunner> runnerList = new ArrayList<>();
            for (int i = 0, c = CollectionUtils.getSize(dataList); i < c; i++) {
                StockCodeBean stockCodeBean = dataList.get(i);
                if (blacklistService.isInBlacklist(stockCodeBean)) { // 如果在黑名单中就不再预测了
                    continue;
                }
                runnerList.add(new PredictionRunner(stockPredictionService, fileService, stockCodeBean, dataDateString, neuralDateString));
            }

            try {
                logger.info("start prediction");
                List<Future<BuyReportBean>> futureList = threadService.getPredictionService().invokeAll(runnerList);
                logger.info("end prediction");

                List<BuyReportBean> buyReportBeanList = new ArrayList<>();
                for (int i = 0; i < futureList.size(); i++) {
                    BuyReportBean buyReportBean = futureList.get(i).get();
                    if (CollectionUtils.getSize(buyReportBean.getPlusList()) > 0
                            && buyReportBean.getFirstChange().compareTo(BigDecimal.ZERO) > 0) {
                        buyReportBeanList.add(buyReportBean);
                    }
                }

                // 根据最后收益变化排序
                Collections.sort(buyReportBeanList, new Comparator<BuyReportBean>() {
                    @Override
                    public int compare(BuyReportBean o1, BuyReportBean o2) {
                        return o1.compareTo(o2);
                    }
                });

                // 最后输出到报表
                StringBuilder buyReport = new StringBuilder();
                for (BuyReportBean buyReportBean: buyReportBeanList) {
                    buyReport.append(buyReportBean.toString()).append(StringUtils.LF);
                }

                FileUtils.writeStringToFile(fileService.getBuyReportFile(dataDateString), buyReport.toString(), Boolean.FALSE, Constants.CHARSET.UTF8);
                logger.info(buyReport.toString());

            } catch (Exception e) {
                logger.error("Run.main invokeAll has error", e);
            }
        } else {
            logger.info("取消机器学习预测");
        }

        // 统计分析预测
        if (analysisStockService.getIsAnalysis()) {
            List<AnalysisRunner> runnerList = new ArrayList<>();
            for (int i = 0, c = CollectionUtils.getSize(dataList); i < c; i++) {
                StockCodeBean stockCodeBean = dataList.get(i);
                if (blacklistService.isInBlacklist(stockCodeBean)) { // 如果在黑名单中就不再预测了
                    continue;
                }
                runnerList.add(new AnalysisRunner(analysisStockService, stockCodeBean, dataDateString));
            }

            try {
                logger.info("start analysis");
                List<Future<LastNUpBean>> futureList = threadService.getPredictionService().invokeAll(runnerList);
                logger.info("end analysis");

                List<LastNUpBean> lastNUpBeanList = new ArrayList<>();
                for (int i = 0; i < futureList.size(); i++) {
                    LastNUpBean lastNUpBean = futureList.get(i).get();
                    if (StringUtils.isNotBlank(lastNUpBean.getDesc())) {
                        lastNUpBeanList.add(lastNUpBean);
                    }
                }

                // 根据最后收益变化排序
                Collections.sort(lastNUpBeanList, new Comparator<LastNUpBean>() {
                    @Override
                    public int compare(LastNUpBean o1, LastNUpBean o2) {
                        return o1.compareTo(o2);
                    }
                });

                // 最后输出到报表
                StringBuilder analysisReport = new StringBuilder();
                for (LastNUpBean lastNUpBean: lastNUpBeanList) {
                    analysisReport.append(lastNUpBean.toString()).append(StringUtils.LF);
                }

                FileUtils.writeStringToFile(fileService.getLastNUpFile(dataDateString), analysisReport.toString(), Boolean.FALSE, Constants.CHARSET.UTF8);
                logger.info(analysisReport.toString());

            } catch (Exception e) {
                logger.error("Run.main invokeAll has error", e);
            }
        } else {
            logger.info("取消统计分析预测");
        }

        // 最后退出
        System.exit(0);
    }
}
