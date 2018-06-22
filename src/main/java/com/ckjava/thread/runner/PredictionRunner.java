package com.ckjava.thread.runner;

import com.ckjava.bean.BuyReportBean;
import com.ckjava.bean.StockCodeBean;
import com.ckjava.service.StockPredictionService;

import java.util.concurrent.Callable;

public class PredictionRunner implements Callable<BuyReportBean> {

    private StockPredictionService stockPredictionService;
    private StockCodeBean stockCodeBean;
    private String dateString;

    public PredictionRunner(StockPredictionService stockPredictionService, StockCodeBean stockCodeBean, String dateString) {
        this.stockPredictionService = stockPredictionService;
        this.stockCodeBean = stockCodeBean;
        this.dateString = dateString;
    }

    @Override
    public BuyReportBean call() throws Exception {
        return stockPredictionService.doPrediction(dateString, stockCodeBean);
    }
}
