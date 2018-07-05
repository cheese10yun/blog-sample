package com.example.pagerduty;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
@Slf4j
public class PagerDutySenderService {

    private final RestTemplate restTemplate;


    public void sendErrorMessage() {
        final PagerDutyDto.Request request = buildErrorMessage();
        send(request);
    }


    private PagerDutyDto.Response send(final PagerDutyDto.Request request) {
        try {
            final String url = "https://events.pagerduty.com/v2/enqueue";
            return restTemplate.postForObject(url, request, PagerDutyDto.Response.class);
        } catch (HttpClientErrorException e) {
            log.error(e.getResponseBodyAsString(), e);
            throw new RuntimeException(e);
        }
    }

    private PagerDutyDto.Request buildErrorMessage() {
        return PagerDutyDto.Request.builder()
                .eventAction(PagerDutyDto.EventAction.acknowledge)
                .build();
    }

}
