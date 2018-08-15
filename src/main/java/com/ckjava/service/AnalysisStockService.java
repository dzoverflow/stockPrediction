package com.ckjava.service;

import com.ckjava.bean.BuyReportBean;
import com.ckjava.bean.LastNUpBean;
import com.ckjava.bean.StockCodeBean;
import com.ckjava.samples.stockmarket.StockInfo;
import com.ckjava.xutils.*;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.learning.TrainingElement;
import org.neuroph.core.learning.TrainingSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.LMS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@PropertySource(value = {"classpath:app.properties"})
public class AnalysisStockService extends FileUtils implements Constants {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisStockService.class);

    @Value("${isAnalysis}")
    private Boolean isAnalysis;
    @Value("${dataDateString}")
    private String dataDateString;
    @Autowired
    private TrainingDataService trainingDataService;

    public String getDataDateString() {
        return dataDateString;
    }

    private NumberFormat nt = NumberFormat.getPercentInstance();

    public Boolean getIsAnalysis() {
        return isAnalysis;
    }

    public LastNUpBean doAnalysis(String dataDateString, StockCodeBean stockCodeBean) {
        LastNUpBean lastNUpBean = new LastNUpBean(stockCodeBean, null);

        // 使用测试集
        // 加载最近的 4 天数据
        TrainingSet testSet = trainingDataService.loadTestTrainingSet(dataDateString, stockCodeBean, 100.00D);
        if (testSet == null || testSet.isEmpty()) {
            return lastNUpBean;
        }

        // 找到最近连续上涨幅度超过 n% 的股票
        String desc = findLastNUp(lastNUpBean, testSet);
        lastNUpBean.setDesc(desc);

        return lastNUpBean;
    }

    private String findLastNUp(LastNUpBean lastNUpBean, TrainingSet testSet) {
        TrainingElement testElement = testSet.trainingElements().get(0);
        Vector<Double> lastTestInput = testElement.getInput();
        double d0 = lastTestInput.get(0);
        double d1 = lastTestInput.get(1);
        double d2 = lastTestInput.get(2);
        double d3 = lastTestInput.get(3);
        if (d0 <= 0 || d1 <= 0 || d2 <= 0 || d3 <= 0) {
            return null;
        }
        BigDecimal up1 = BigDecimal.valueOf(d1).subtract(BigDecimal.valueOf(d0)).divide(BigDecimal.valueOf(d0), 4, BigDecimal.ROUND_HALF_UP);
        BigDecimal up2 = BigDecimal.valueOf(d2).subtract(BigDecimal.valueOf(d1)).divide(BigDecimal.valueOf(d1), 4, BigDecimal.ROUND_HALF_UP);
        BigDecimal up3 = BigDecimal.valueOf(d3).subtract(BigDecimal.valueOf(d2)).divide(BigDecimal.valueOf(d2), 4, BigDecimal.ROUND_HALF_UP);
        StringBuilder data = new StringBuilder();

        lastNUpBean.setLast1(up1);

        // 连续2天的涨幅, 分别超过 1% 到 10%
        BigDecimal upFactor = BigDecimal.ZERO;
        for (int i = 1; i <= 10; i++) {
            BigDecimal up = BigDecimal.valueOf(i/100D);
            if (up2.compareTo(up) >= 0 && up3.compareTo(up) >= 0) {
                upFactor = up;
            }
        }

        if (upFactor.compareTo(BigDecimal.ZERO) > 0) {
            lastNUpBean.setLast2(upFactor);
            data.append("连续2天上涨:" + nt.format(upFactor));
        }

        // 连续3天的涨幅, 分别超过 1% 到 10%
        for (int i = 1; i <= 10; i++) {
            BigDecimal up = BigDecimal.valueOf(i/100D);
            if (up1.compareTo(up) >= 0
                    && up2.compareTo(up) >= 0
                    && up3.compareTo(up) >= 0) {
                upFactor = up;
            }
        }

        if (upFactor.compareTo(BigDecimal.ZERO) > 0) {
            lastNUpBean.setLast3(upFactor);
            data.append(", 连续3天上涨:" + nt.format(upFactor));
        }

        return data.toString();
    }
}
