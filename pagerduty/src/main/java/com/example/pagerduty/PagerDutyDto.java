package com.example.pagerduty;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class PagerDutyDto {


    public enum Severity {
        info, error, warn
    }


    public enum Source {
        SODA_TRANSFER
    }


    public enum Group {
        PROD,
        TEST
    }

    public enum EventAction {
        trigger, acknowledge, resolve
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Request {
        @JsonProperty("event_action")
        private EventAction eventAction;
        @JsonProperty("routing_key")
        private String routingKey = "routingKey...";
        private Payload payload;

        @Builder
        public Request(EventAction eventAction, Payload payload) {
            this.eventAction = eventAction;
            this.payload = payload;
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Response {
        private String status;
        private String message;
        @JsonProperty("dedup_key")
        private String dedupKey;

    }


    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Payload {
        private String summary;
        private String timestamp = ZonedDateTime.now().toOffsetDateTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        private Severity severity;
        private Group group;
        private Source source;
        @JsonProperty("custom_details")
        private Object customDetails;

        @Builder
        public Payload(String summary, Severity severity, Group group, Source source, Object customDetails) {
            this.summary = summary;
            this.severity = severity;
            this.group = group;
            this.source = source;
            this.customDetails = customDetails;
        }
    }
}

