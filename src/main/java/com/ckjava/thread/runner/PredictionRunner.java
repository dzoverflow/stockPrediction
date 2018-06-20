package com.ckjava.thread.runner;

import com.ckjava.bean.StockCodeBean;
import com.ckjava.service.StockPredictionService;

import java.util.concurrent.Callable;

public class PredictionRunner implements Callable<String> {

    private StockPredictionService stockPredictionService;
    private StockCodeBean stockCodeBean;

    public PredictionRunner(StockPredictionService stockPredictionService, StockCodeBean stockCodeBean) {
        this.stockPredictionService = stockPredictionService;
        this.stockCodeBean = stockCodeBean;
    }

    @Override
    public String call() throws Exception {
        return stockPredictionService.doPrediction(stockCodeBean);
    }
}
