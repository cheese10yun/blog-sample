package com.cheese.embedded.repository;

import com.cheese.embedded.domain.Sender;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SenderRepository extends JpaRepository<Sender, Long> {
}
