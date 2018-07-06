package com.example.pagerduty;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class PagerDutyDto {


    public enum Severity {
        info, error, warn
    }


    public enum Source {
        XXX_SERVICE
    }


    public enum Group {
        PROD,
        TEST
    }

    public enum EventAction {
        trigger, acknowledge, resolve
    }

    @Getter
    public static class Request {
        @JsonProperty("event_action")
        private final EventAction eventAction;
        @JsonProperty("routing_key")
        private final String routingKey = "routingKey...";
        private final Payload payload;

        @Builder
        public Request(final EventAction eventAction, final Payload payload) {
            this.eventAction = eventAction;
            this.payload = payload;
        }
    }

    @Getter
    public static class Response {
        private String status;
        private String message;
        @JsonProperty("dedup_key")
        private String dedupKey;

    }


@Getter
public static class Payload {
    private final String summary;
    private final String timestamp = ZonedDateTime.now().toOffsetDateTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    private final Severity severity;
    private final Group group;
    private final Source source;
    @JsonProperty("custom_details")
    private final Object customDetails;

    @Builder
    public Payload(final String summary, final Severity severity, final Group group, final Source source, final Object customDetails) {
        this.summary = summary;
        this.severity = severity;
        this.group = group;
        this.source = source;
        this.customDetails = customDetails;
    }
}
}

