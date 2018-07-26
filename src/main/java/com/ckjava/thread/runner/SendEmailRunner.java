package com.ckjava.thread.runner;

import com.ckjava.email.EmailService;
import com.ckjava.email.MailInfoVo;
import com.ckjava.xutils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class SendEmailRunner implements Callable<Boolean>, Constants {

    private static final Logger logger = LoggerFactory.getLogger(SendEmailRunner.class);

    private EmailService emailService;
    private MailInfoVo mailInfoVo;

    public SendEmailRunner(EmailService emailService, MailInfoVo mailInfoVo) {
        this.emailService = emailService;
        this.mailInfoVo = mailInfoVo;
    }

    @Override
    public Boolean call() throws Exception {
        boolean flag = emailService.sendEmail(mailInfoVo.getMailTitle(), mailInfoVo.getMailContent());
        logger.info("send email {}", (flag? STATUS.SUCCESS:STATUS.FAIL));
        return flag;
    }
}
