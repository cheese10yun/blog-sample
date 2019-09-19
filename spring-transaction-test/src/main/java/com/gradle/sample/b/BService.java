package com.gradle.sample.b;

import com.gradle.sample.a.A;
import com.gradle.sample.a.AService;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
//@Transactional
@RequiredArgsConstructor
@Slf4j
public class BService {

    private final BRepository bRepository;

    public A bCreate(final A a) {
        a.setName("1111");
        log.error("B => currentTransactionName : {}", TransactionSynchronizationManager.getCurrentTransactionName());
        bRepository.save(new B());

        if(true){
            throw new RuntimeException();
        }

        return a;
    }
}
