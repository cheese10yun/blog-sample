package com.cheese.notification.kakao;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class KakaoNotification {

    @Id
    @GeneratedValue
    private long id;

    @Column(name = "subject")
    private String subject;

    @Column(name = "content")
    private String content;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "template_code")
    private String templateCode;

    @Builder
    public KakaoNotification(String subject, String content, String templateCode, String mobile) {
        this.subject = subject;
        this.content = content;
        this.templateCode = templateCode;
        this.mobile = mobile;
    }
}
