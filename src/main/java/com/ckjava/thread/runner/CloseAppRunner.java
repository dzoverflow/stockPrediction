package com.ckjava.thread.runner;

import com.ckjava.service.ThreadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloseAppRunner implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CloseAppRunner.class);

    private ThreadService threadService;

    public CloseAppRunner(ThreadService threadService) {
        this.threadService = threadService;
    }

    @Override
    public void run() {
        threadService.shutDownAllService();

        do {
            if (threadService.isAllServiceDead()) {
                System.exit(0);
            }
            try {
                Thread.currentThread().sleep(1000);
            } catch (Exception e) {
                logger.error(this.getClass().getName().concat(".sleep has error"), e);
            }
        } while (true);
    }
}
