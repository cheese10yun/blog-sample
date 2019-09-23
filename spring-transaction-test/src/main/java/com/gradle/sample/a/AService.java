package com.gradle.sample.a;

import com.gradle.sample.b.BService;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AService {

    private final ARepository aRepository;
    private final BService bService;
    private final EntityManager entityManager;

    public A aCreate() {
        entityManager.clear();
        final A a = aRepository.save(new A());
        log.error("A -> currentTransactionName : {}", TransactionSynchronizationManager.getCurrentTransactionName());
        bService.bCreate(a);
        return a;
    }
}
