package com.cheese.embedded.repository;

import com.cheese.embedded.domain.Receiver;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiverRepository extends JpaRepository<Receiver, Long> {
}
