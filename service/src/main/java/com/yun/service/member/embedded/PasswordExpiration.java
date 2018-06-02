package com.yun.service.member.embedded;


import lombok.Builder;
import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.time.LocalDateTime;

@Embeddable
@Getter
public class PasswordExpiration {

    @Column(name = "password_expiration_date")
    private LocalDateTime expirationDate;

    @Column(name = "password_ttl")
    private long ttl;

    @Builder
    protected PasswordExpiration() {
        this.ttl = 1209_604; //14 days
        this.expirationDate = extend();
    }

    public LocalDateTime extend() {
        return LocalDateTime.now().plusSeconds(ttl);
    }

    public boolean isExpiration() {
        return LocalDateTime.now().isAfter(expirationDate);
    }
}
