package com.ckjava;

import com.ckjava.bean.BuyReportBean;
import com.ckjava.bean.StockCodeBean;
import com.ckjava.service.*;
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

        String dateString = downloadFileService.getDateString();
        if (StringUtils.isBlank(dateString)) {
            dateString = DateUtils.formatTime(new Date().getTime(), "yyyyMMdd");
        }

        // 初始化黑名单
        List<StockCodeBean> blacklist = blacklistService.initBlacklist();
        // 读取股票编码
        List<StockCodeBean> dataList = downloadFileService.getStockCodeList("sh");

        // 下载股票数据文件
        if (downloadFileService.getIsDownloadRawFile()) {
            for (int i = 0, c = CollectionUtils.getSize(dataList); i < c; i++) {
                StockCodeBean stockCodeBean = dataList.get(i);
                if (blacklistService.isInBlacklist(blacklist, stockCodeBean)) { // 如果在黑名单中就不再下载了
                    continue;
                }

                downloadFileService.downloadStockDataFile(dateString, stockCodeBean);
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

        // 分析预测
        if (stockPredictionService.getIsPrediction()) {
            List<PredictionRunner> runnerList = new ArrayList<>();
            for (int i = 0, c = CollectionUtils.getSize(dataList); i < c; i++) {
                StockCodeBean stockCodeBean = dataList.get(i);
                runnerList.add(new PredictionRunner(stockPredictionService, stockCodeBean, dateString));
            }

            try {
                logger.info("start prediction");
                List<Future<BuyReportBean>> futureList = threadService.getPredictionService().invokeAll(runnerList);
                logger.info("end prediction");

                List<BuyReportBean> buyReportBeanList = new ArrayList<>();
                for (int i = 0; i < futureList.size(); i++) {
                    buyReportBeanList.add(futureList.get(i).get());
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
                    if (CollectionUtils.getSize(buyReportBean.getPlusList()) > 0
                            && buyReportBean.getTotalChange().compareTo(BigDecimal.ZERO) > 0) {
                        buyReport.append(buyReportBean.toString()).append(StringUtils.LF);
                    }
                }

                FileUtils.writeStringToFile(fileService.getBuyReportFile(dateString), buyReport.toString(), Boolean.FALSE, Constants.CHARSET.UTF8);
                logger.info(buyReport.toString());

            } catch (Exception e) {
                logger.error("Run.main invokeAll has error", e);
            }
        } else {
            logger.info("取消分析预测");
        }

        // 最后退出
        System.exit(0);
    }
}
