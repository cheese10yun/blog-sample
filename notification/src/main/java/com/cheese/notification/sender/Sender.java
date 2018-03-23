package com.cheese.notification.sender;

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
public class Sender {

    @Id
    @GeneratedValue
    private long id;

    private String name;
    private String contact;

    @Builder
    public Sender(String name, String contact) {
        this.name = name;
        this.contact = contact;
    }
}
