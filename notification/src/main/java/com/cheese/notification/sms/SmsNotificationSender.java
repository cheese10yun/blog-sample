package com.cheese.notification.sms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SmsNotificationSender {
    private final RestTemplate restTemplate;

    @Autowired
    public SmsNotificationSender(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendSMS(SmsMessageDto.Creation dto) {
        System.out.println("SMS API Call..");
        HttpEntity<SmsMessageDto.Creation> request = new HttpEntity<>(dto, getHeader());
        restTemplate.exchange("url...", HttpMethod.POST, request, String.class);
    }

    private HttpHeaders getHeader() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "....");
        return httpHeaders;
    }
}
