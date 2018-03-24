package com.cheese.notification.kakao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class KakaoNotificaionSender {

    private KakaoNotificationRepository kakaoNotificationRepository;

    @Autowired
    public KakaoNotificaionSender(KakaoNotificationRepository kakaoNotificationRepository) {
        this.kakaoNotificationRepository = kakaoNotificationRepository;
    }

    public final KakaoNotification create(KakaoNotificationDto.Creation dto) {
        System.out.println("kakao message insert...");
        return kakaoNotificationRepository.save(dto.toEntity());
    }

}
