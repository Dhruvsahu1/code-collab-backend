package com.codesync.notification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${spring.mail.properties.mail.smtp.auth}")
    private String smtpAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private String starttlsEnable;

    @Value("${spring.mail.properties.mail.smtp.connectiontimeout}")
    private String connectionTimeout;

    @Value("${spring.mail.properties.mail.smtp.timeout}")
    private String timeout;

    @Value("${spring.mail.properties.mail.smtp.writetimeout}")
    private String writeTimeout;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties javaMailProperties = new Properties();
        javaMailProperties.put("mail.smtp.auth", smtpAuth);
        javaMailProperties.put("mail.smtp.starttls.enable", starttlsEnable);
        javaMailProperties.put("mail.smtp.connectiontimeout", connectionTimeout);
        javaMailProperties.put("mail.smtp.timeout", timeout);
        javaMailProperties.put("mail.smtp.writetimeout", writeTimeout);

        mailSender.setJavaMailProperties(javaMailProperties);
        return mailSender;
    }
}