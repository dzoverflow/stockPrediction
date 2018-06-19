package com.ckjava;

import com.ckjava.bean.StockCodeBean;
import com.ckjava.service.FileDownloadService;
import com.ckjava.service.StockPredictionService;
import com.ckjava.xutils.CollectionUtils;
import com.ckjava.xutils.Constants;
import com.ckjava.xutils.FileUtils;
import com.ckjava.xutils.StringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.util.List;

public class Run {

    private static final Logger logger = LoggerFactory.getLogger(Run.class);

    private static ApplicationContext appc;

    public static void main(String[] args) {
        appc = new AnnotationConfigApplicationContext("com.ckjava");
        FileDownloadService fileDownloadService = appc.getBean(FileDownloadService.class);
        StockPredictionService stockPredictionService = appc.getBean(StockPredictionService.class);
        // 读取股票编码
        List<StockCodeBean> dataList = fileDownloadService.getStockCodeList("sh");

        // 下载股票数据文件
        for (int i = 0, c = CollectionUtils.getSize(dataList); i < c; i++) {
            StockCodeBean stockCodeBean = dataList.get(i);

            fileDownloadService.downloadStockDataFile(stockCodeBean);
            try {
                long sleepTime = RandomUtils.nextLong(100, 2000);
                Thread.currentThread().sleep(sleepTime);
            } catch (Exception e) {
                logger.error(Run.class.getName().concat(".main has error"), e);
            }

        }

        // 分析预测
        StringBuilder buyReport = new StringBuilder();
        for (int i = 0, c = CollectionUtils.getSize(dataList); i < c; i++) {
            StockCodeBean stockCodeBean = dataList.get(i);
            String simpleReport = stockPredictionService.doPrediction(stockCodeBean);
            buyReport.append(simpleReport).append(StringUtils.LF);
        }

        String buyReportFilePath = stockPredictionService.getDataPath() + "buyReport_" + stockPredictionService.getCurrentDateString() + ".txt";
        FileUtils.createFile(buyReportFilePath);

        FileUtils.writeStringToFile(new File(buyReportFilePath), buyReport.toString(), Boolean.FALSE, Constants.CHARSET.UTF8);
        logger.info(buyReport.toString());
    }
}
