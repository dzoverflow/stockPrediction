package com.ckjava.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ThreadService {

    private static final int threads = Runtime.getRuntime().availableProcessors()-1;
    private static ExecutorService predictionService = Executors.newFixedThreadPool(threads);
    private static ExecutorService sendEmailService = Executors.newFixedThreadPool(threads);
    private static ExecutorService closeAppService = Executors.newSingleThreadExecutor();

    public ExecutorService getPredictionService() {
        return predictionService;
    }

    public ExecutorService getSendEmailService() {
        return sendEmailService;
    }

    public ExecutorService getCloseAppService() {
        return closeAppService;
    }

    public void shutDownAllService() {
        getPredictionService().shutdown();
        getSendEmailService().shutdown();
    }

    public boolean isAllServiceDead() {
        boolean flag = true;
        if (!getPredictionService().isTerminated()) {
            flag = false;
        }
        if (!getSendEmailService().isTerminated()) {
            flag = false;
        }
        return flag;
    }
}
