package com.ckjava.thread.runner;

import com.ckjava.bean.BuyReportBean;
import com.ckjava.bean.StockCodeBean;
import com.ckjava.service.FileService;
import com.ckjava.service.StockPredictionService;
import com.ckjava.xutils.CollectionUtils;
import com.ckjava.xutils.Constants;
import com.ckjava.xutils.FileUtils;
import com.ckjava.xutils.StringUtils;

import java.math.BigDecimal;
import java.util.concurrent.Callable;

public class PredictionRunner extends FileUtils implements Callable<BuyReportBean>, Constants {

    private StockPredictionService stockPredictionService;
    private FileService fileService;
    private StockCodeBean stockCodeBean;
    private String dataDateString;
    private String neuralDateString;

    public PredictionRunner(StockPredictionService stockPredictionService, FileService fileService, StockCodeBean stockCodeBean, String dataDateString, String neuralDateString) {
        this.stockPredictionService = stockPredictionService;
        this.fileService = fileService;
        this.stockCodeBean = stockCodeBean;
        this.dataDateString = dataDateString;
        this.neuralDateString = neuralDateString;
    }

    @Override
    public BuyReportBean call() throws Exception {
        BuyReportBean buyReportBean = stockPredictionService.doPrediction(dataDateString, neuralDateString, stockCodeBean);
        if (CollectionUtils.getSize(buyReportBean.getPlusList()) > 0
                && buyReportBean.getTotalChange().compareTo(BigDecimal.ZERO) > 0) {

            FileUtils.writeStringToFile(fileService.getBuyReportFile(dataDateString), buyReportBean.toString().concat(StringUtils.LF), Boolean.TRUE, Constants.CHARSET.UTF8);
        }

        return buyReportBean;
    }
}
