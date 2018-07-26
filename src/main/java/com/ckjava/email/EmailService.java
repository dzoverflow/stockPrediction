package com.ckjava.email;

import com.ckjava.xutils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

@Service
public class EmailService implements Constants {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Value("${mailFrom}")
    private String mailFrom;
    @Value("${mailFromName}")
    private String mailFromName;
    @Value("${mailTo}")
    private String mailTo;
    @Value("${smtpHost}")
    private String smtpHost;
    @Value("${smtpPass}")
    private String smtpPass;
    @Value("${smtpPort}")
    private Integer smtpPort;
    @Value("${smtpSslPort}")
    private Integer smtpSslPort;

    private Session getMailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost); //SMTP Host
        props.put("mail.smtp.socketFactory.port", smtpSslPort); //SSL Port
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); //SSL Factory Class
        props.put("mail.smtp.auth", "true"); //Enabling SMTP Authentication
        props.put("mail.smtp.port", smtpPort); //SMTP Port

        Authenticator auth = new Authenticator() {
            //override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailFrom, smtpPass);
            }
        };

        return Session.getDefaultInstance(props, auth);
    }

    /**
     * Utility method to send simple HTML email
     *
     * @param subject
     * @param body
     */
    public void sendEmail(String subject, String body) {
        try {
            Session session = getMailSession();
            MimeMessage msg = new MimeMessage(session);
            //set message headers
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");

            msg.setFrom(new InternetAddress(mailFrom, mailFromName));
            msg.setReplyTo(InternetAddress.parse(mailFrom, false));
            msg.setSubject(subject, CHARSET.UTF8);
            msg.setText(body, CHARSET.UTF8);

            msg.setSentDate(new Date());

            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailTo, false));
            logger.info("Message is ready");
            Transport.send(msg);
            logger.info("EMail Sent Successfully!!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
