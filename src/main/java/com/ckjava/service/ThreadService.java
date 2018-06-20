package com.ckjava.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ThreadService {

    private static final int threads = Runtime.getRuntime().availableProcessors()-1;
    private static ExecutorService predictionService = Executors.newFixedThreadPool(threads);

    public ExecutorService getPredictionService() {
        return predictionService;
    }
}
