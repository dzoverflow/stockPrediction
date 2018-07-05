package com.ckjava.thread.runner;

import com.ckjava.bean.LastNUpBean;
import com.ckjava.bean.StockCodeBean;
import com.ckjava.service.AnalysisStockService;
import com.ckjava.xutils.Constants;

import java.util.concurrent.Callable;

public class AnalysisRunner implements Callable<LastNUpBean>, Constants {

    private AnalysisStockService analysisStockService;
    private StockCodeBean stockCodeBean;
    private String dataDateString;

    public AnalysisRunner(AnalysisStockService analysisStockService, StockCodeBean stockCodeBean, String dataDateString) {
        this.analysisStockService = analysisStockService;
        this.stockCodeBean = stockCodeBean;
        this.dataDateString = dataDateString;
    }

    @Override
    public LastNUpBean call() throws Exception {
        return analysisStockService.doAnalysis(dataDateString, stockCodeBean);
    }
}
