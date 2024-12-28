package com.xplore.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailSenderService.class);

    @Autowired
    private JavaMailSender javaMailSender;

    public ResponseEntity<String> sendMail(
        String to,
        String subject,
        String message
    ){
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        logger.info("Mail sending service using...");
        mailMessage.setTo(to);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);
        javaMailSender.send(mailMessage);
        return new ResponseEntity<>("Your Email Is Sending...",HttpStatus.OK);
    }

}
