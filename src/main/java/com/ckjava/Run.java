package com.ckjava;

import com.ckjava.bean.BuyReportBean;
import com.ckjava.bean.LastNBean;
import com.ckjava.bean.StockCodeBean;
import com.ckjava.email.EmailService;
import com.ckjava.email.MailInfoVo;
import com.ckjava.service.*;
import com.ckjava.thread.runner.AnalysisRunner;
import com.ckjava.thread.runner.CloseAppRunner;
import com.ckjava.thread.runner.PredictionRunner;
import com.ckjava.thread.runner.SendEmailRunner;
import com.ckjava.xutils.CollectionUtils;
import com.ckjava.xutils.DateUtils;
import com.ckjava.xutils.FileUtils;
import com.ckjava.xutils.StringUtils;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.MessageFormat;
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
        EmailService emailService = appc.getBean(EmailService.class);
        FreeMarkerConfigurer freeMarkerConfigurer = appc.getBean(FreeMarkerConfigurer.class);

        String downloadDateString = downloadFileService.getDownloadDateString();
        if (StringUtils.isBlank(downloadDateString)) {
            downloadDateString = DateUtils.formatTime(new Date().getTime(), "yyyyMMdd");
        }

        // 读取股票编码
        List<StockCodeBean> dataList = downloadFileService.getStockCodeList();
        // 遇到下载异常需要重试的列表
        List<StockCodeBean> retryDataList = new ArrayList<>();

        // 下载股票数据文件
        if (downloadFileService.getIsDownloadRawFile()) {
            for (int i = 0, c = CollectionUtils.getSize(dataList); i < c; i++) {
                StockCodeBean stockCodeBean = dataList.get(i);
                // 如果在黑名单中就不再下载了
                if (blacklistService.isInBlacklist(stockCodeBean)) {
                    continue;
                }
                // 如果已经下载过了就不再下载
                File rawDataFile = fileService.getRawDataFile(downloadDateString, stockCodeBean.getArea(), stockCodeBean.getCode());
                if (rawDataFile.exists()) {
                    continue;
                }
                // 执行下载
                boolean flag = downloadFileService.downloadStockDataFile(downloadDateString, stockCodeBean);
                if (flag) {
                    // 下载完毕后随机等待一下
                    randomSleep();
                } else {
                    retryDataList.add(stockCodeBean);
                }
            }

            // 对于下载中遇到异常的股票重试3次下载
            List<StockCodeBean> retrySuccessDataList = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                for (int j = 0, c = CollectionUtils.getSize(retryDataList); j < c; j++) {
                    StockCodeBean stockCodeBean = retryDataList.get(j);
                    if (retrySuccessDataList.contains(stockCodeBean)) {
                        continue;
                    }
                    boolean flag = downloadFileService.downloadStockDataFile(downloadDateString, stockCodeBean);
                    if (flag) {
                        retrySuccessDataList.add(stockCodeBean);
                        randomSleep();
                    }
                }
            }
            for (int j = 0, c = CollectionUtils.getSize(retryDataList); j < c; j++) {
                StockCodeBean stockCodeBean = retryDataList.get(j);
                logger.info("下载数据失败的股票为 {}", stockCodeBean.toString());
            }

        } else {
            logger.info("取消下载股票数据文件");
        }

        // 删除前一天的数据文件
        try {
            Date lastDate = DateUtils.getAssignDay(new Date(), -1);
            String lastDateFilePath = fileService.getFilePath(DateUtils.formatTime(lastDate.getTime(), "yyyyMMdd")).getAbsolutePath();
            boolean flag = deleteDirectory(lastDateFilePath);
            System.out.println(flag);
            logger.info("deleteDirectory {} success", lastDateFilePath);
        } catch (Exception e) {
            logger.error(Run.class.getName().concat(".deleteDirectory has error"), e);
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

                FileUtils.writeStringToFile(fileService.getBuyReportFile(dataDateString), buyReport.toString(), Boolean.FALSE, CHARSET.UTF8);
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
                List<Future<LastNBean>> futureList = threadService.getPredictionService().invokeAll(runnerList);
                logger.info("end analysis");

                // 上涨的情况
                int upCount = 0;
                List<LastNBean> lastNUpBeanList = new ArrayList<>();
                for (int i = 0; i < futureList.size(); i++) {
                    LastNBean lastNBean = futureList.get(i).get();
                    if (StringUtils.isNotBlank(lastNBean.getUpDesc())) {
                        lastNUpBeanList.add(lastNBean);
                    }
                    if (lastNBean.getUp1() != null) {
                        if (lastNBean.getUp1().compareTo(BigDecimal.ZERO) == 1) {
                            upCount ++;
                        }
                    } else {
                        logger.warn("异常股票信息 {}", lastNBean.toString());
                    }
                }

                // 根据最后收益变化排序
                Collections.sort(lastNUpBeanList, new Comparator<LastNBean>() {
                    @Override
                    public int compare(LastNBean o1, LastNBean o2) {
                        return o1.compareUpTo(o2);
                    }
                });

                // 最后输出到报表
                StringBuilder analysisReport = new StringBuilder();
                for (LastNBean lastNBean : lastNUpBeanList) {
                    analysisReport.append(lastNBean.toUpString()).append(StringUtils.LF);
                }

                FileUtils.writeStringToFile(fileService.getLastNUpFile(dataDateString), analysisReport.toString(), Boolean.FALSE, CHARSET.UTF8);
                logger.info(analysisReport.toString());

                // 发送邮件
                String dateTitle = DateUtils.formatTime(new Date().getTime(), "yyyy年MM月dd日");
                String mailTitle = dateTitle.concat(" 沪深个股上涨情况统计分析");
                StringBuilder mailContent = new StringBuilder();
                mailContent.append(MessageFormat.format("统计个股数{0}, 上涨个股数{1}", new Object[]{ futureList.size(), upCount })).append(StringUtils.LF);
                mailContent.append("区域,股票, 连续2天上涨,连续3天上涨").append(StringUtils.LF);
                mailContent.append(CollectionUtils.getSize(lastNUpBeanList) >0 ? analysisReport.toString() : "no data").append(StringUtils.LF);
                mailContent.append("注意：根据历史数据统计股票上涨情况，连续3天是指该股票今天，昨天，前天的涨幅超过了%多少。");

                MailInfoVo mailInfoVo = new MailInfoVo();
                mailInfoVo.setMailTitle(mailTitle);
                mailInfoVo.setMailContent(mailContent.toString());
                threadService.getSendEmailService().submit(new SendEmailRunner(emailService, mailInfoVo));

                // 下跌情况
                int downCount = 0;
                List<LastNBean> lastNDownBeanList = new ArrayList<>();
                for (int i = 0; i < futureList.size(); i++) {
                    LastNBean lastNBean = futureList.get(i).get();
                    if (StringUtils.isNotBlank(lastNBean.getDownDesc())) {
                        lastNDownBeanList.add(lastNBean);
                    }
                    if (lastNBean.getUp1() != null) {
                        if (lastNBean.getUp1().compareTo(BigDecimal.ZERO) == -1) {
                            downCount ++;
                        }
                    } else {
                        logger.warn("异常股票信息 {}", lastNBean.toString());
                    }
                }

                // 根据最后收益变化排序
                Collections.sort(lastNDownBeanList, new Comparator<LastNBean>() {
                    @Override
                    public int compare(LastNBean o1, LastNBean o2) {
                        return o1.compareDownTo(o2);
                    }
                });

                // 最后输出到报表
                analysisReport = new StringBuilder();
                for (LastNBean lastNBean : lastNDownBeanList) {
                    analysisReport.append(lastNBean.toDownString()).append(StringUtils.LF);
                }

                FileUtils.writeStringToFile(fileService.getLastNDownFile(dataDateString), analysisReport.toString(), Boolean.FALSE, CHARSET.UTF8);
                logger.info(analysisReport.toString());

                // 发送邮件
                dateTitle = DateUtils.formatTime(new Date().getTime(), "yyyy年MM月dd日");
                mailTitle = dateTitle.concat(" 沪深个股下跌情况统计分析");
                mailContent = new StringBuilder();
                mailContent.append(MessageFormat.format("统计个股数{0}, 下跌个股数{1}", new Object[]{ futureList.size(), downCount })).append(StringUtils.LF);
                mailContent.append("区域,股票, 连续2天下跌,连续3天下跌").append(StringUtils.LF);
                mailContent.append(CollectionUtils.getSize(lastNDownBeanList) >0 ? analysisReport.toString() : "no data").append(StringUtils.LF);
                mailContent.append("注意：根据历史数据统计股票下跌情况，连续3天是指该股票今天，昨天，前天的跌幅超过了%多少。");

                mailInfoVo = new MailInfoVo();
                mailInfoVo.setMailTitle(mailTitle);
                mailInfoVo.setMailContent(mailContent.toString());
                threadService.getSendEmailService().submit(new SendEmailRunner(emailService, mailInfoVo));
            } catch (Exception e) {
                logger.error("Run.main invokeAll has error", e);
            }
        } else {
            logger.info("取消统计分析预测");
        }

        // 最后退出
        threadService.getCloseAppService().submit(new CloseAppRunner(threadService));
    }

    private static void randomSleep() {
        try {
            long sleepTime = RandomUtils.nextLong(100, 2000);
            Thread.currentThread().sleep(sleepTime);
        } catch (Exception e) {
            logger.error(Run.class.getName().concat(".main has error"), e);
        }
    }

    public static String getMailContent(FreeMarkerConfigurer freeMarkerConfigurer, String title, String content, String templateName) throws IOException, TemplateException {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("title", title);
        dataMap.put("content", content);

        Template tpl = freeMarkerConfigurer.getConfiguration().getTemplate(templateName);
        return FreeMarkerTemplateUtils.processTemplateIntoString(tpl, dataMap);
    }
}
