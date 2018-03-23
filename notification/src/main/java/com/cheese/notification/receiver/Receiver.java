package com.cheese.notification.receiver;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Receiver {

    @Id
    @GeneratedValue
    private long id;

    private String name;
    private String email;
    private String mobile;
    private String location;

    @Builder
    public Receiver(String name, String email, String mobile, String location) {
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.location = location;
    }
}
