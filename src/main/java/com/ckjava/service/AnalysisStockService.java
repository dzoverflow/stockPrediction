package com.ckjava.service;

import com.ckjava.bean.LastNBean;
import com.ckjava.bean.StockCodeBean;
import com.ckjava.xutils.*;
import com.ckjava.xutils.http.Page;
import org.neuroph.core.learning.TrainingElement;
import org.neuroph.core.learning.TrainingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
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

    public LastNBean doAnalysis(String dataDateString, StockCodeBean stockCodeBean) {
        LastNBean lastNBean = new LastNBean(stockCodeBean);

        // 使用测试集
        // 加载最近的 4 天数据
        TrainingSet testSet = trainingDataService.loadTestTrainingSet(dataDateString, stockCodeBean, 100.00D);
        if (testSet == null || testSet.isEmpty()) {
            return lastNBean;
        }

        // 找到最近连续上涨幅度超过 n% 的股票
        lastNBean.setUpDesc(findLastNUp(lastNBean, testSet));

        // 找到最近连续下跌幅度超过 n% 的股票
        lastNBean.setDownDesc(findLastNDown(lastNBean, testSet));

        return lastNBean;
    }

    private String findLastNUp(LastNBean lastNBean, TrainingSet testSet) {
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

        lastNBean.setUp1(up1);

        // 连续2天的涨幅, 分别超过 1% 到 10%
        BigDecimal upFactor = BigDecimal.ZERO;
        for (int i = 1; i <= 10; i++) {
            BigDecimal up = BigDecimal.valueOf(i/100D);
            if (up2.compareTo(up) >= 0 && up3.compareTo(up) >= 0) {
                upFactor = up;
            }
        }

        if (upFactor.compareTo(BigDecimal.ZERO) > 0) {
            lastNBean.setUp2(upFactor);
            data.append("连续2天上涨:" + nt.format(upFactor));
        }

        // 连续3天的涨幅, 分别超过 1% 到 10%
        upFactor = BigDecimal.ZERO;
        for (int i = 1; i <= 10; i++) {
            BigDecimal up = BigDecimal.valueOf(i/100D);
            if (up1.compareTo(up) >= 0
                    && up2.compareTo(up) >= 0
                    && up3.compareTo(up) >= 0) {
                upFactor = up;
            }
        }

        if (upFactor.compareTo(BigDecimal.ZERO) > 0) {
            lastNBean.setUp3(upFactor);
            data.append(", 连续3天上涨:" + nt.format(upFactor));
        }

        return data.toString();
    }

    private String findLastNDown(LastNBean lastNBean, TrainingSet testSet) {
        TrainingElement testElement = testSet.trainingElements().get(0);
        Vector<Double> lastTestInput = testElement.getInput();
        double d0 = lastTestInput.get(0);
        double d1 = lastTestInput.get(1);
        double d2 = lastTestInput.get(2);
        double d3 = lastTestInput.get(3);
        if (d0 == 0 || d1 == 0 || d2 == 0 || d3 == 0) {
            return null;
        }
        BigDecimal down1 = BigDecimal.valueOf(d1).subtract(BigDecimal.valueOf(d0)).divide(BigDecimal.valueOf(d0), 4, BigDecimal.ROUND_HALF_UP);
        BigDecimal down2 = BigDecimal.valueOf(d2).subtract(BigDecimal.valueOf(d1)).divide(BigDecimal.valueOf(d1), 4, BigDecimal.ROUND_HALF_UP);
        BigDecimal down3 = BigDecimal.valueOf(d3).subtract(BigDecimal.valueOf(d2)).divide(BigDecimal.valueOf(d2), 4, BigDecimal.ROUND_HALF_UP);

        if (down1.compareTo(BigDecimal.ZERO) >= 0
                || down2.compareTo(BigDecimal.ZERO) >= 0
                || down3.compareTo(BigDecimal.ZERO) >= 0) {
            return null;
        }

        down1 = down1.negate();
        down2 = down2.negate();
        down3 = down3.negate();

        StringBuilder data = new StringBuilder();

        lastNBean.setDown1(down1);

        // 连续2天的跌幅, 分别超过 -1% 到 -10%
        BigDecimal downFactor = BigDecimal.ZERO;
        for (int i = 1; i <= 10; i++) {
            BigDecimal down = BigDecimal.valueOf(i/100D);
            if (down2.compareTo(down) >= 0
                    && down3.compareTo(down) >= 0) {
                downFactor = down;
            }
        }

        downFactor = downFactor.negate();
        if (downFactor.compareTo(BigDecimal.ZERO) < 0) {
            lastNBean.setDown2(downFactor);
            data.append("连续2天下跌:" + nt.format(downFactor));
        }

        // 连续3天的跌幅, 分别超过 -1% 到 -10%
        downFactor = BigDecimal.ZERO;
        for (int i = 1; i <= 10; i++) {
            BigDecimal down = BigDecimal.valueOf(i/100D);
            if (down1.compareTo(down) >= 0
                    && down2.compareTo(down) >= 0
                    && down3.compareTo(down) >= 0) {
                downFactor = down;
            }
        }

        downFactor = downFactor.negate();
        if (downFactor.compareTo(BigDecimal.ZERO) < 0) {
            lastNBean.setDown3(downFactor);
            data.append(", 连续3天下跌:" + nt.format(downFactor));
        }

        return data.toString();
    }
}
