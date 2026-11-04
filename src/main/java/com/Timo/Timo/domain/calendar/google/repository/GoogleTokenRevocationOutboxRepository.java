package com.Timo.Timo.domain.calendar.google.repository;

import com.Timo.Timo.domain.calendar.google.entity.GoogleTokenRevocationOutbox;
import com.Timo.Timo.domain.calendar.google.enums.RevocationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoogleTokenRevocationOutboxRepository extends
    JpaRepository<GoogleTokenRevocationOutbox, Long> {

  List<GoogleTokenRevocationOutbox> findTop50ByStatus(RevocationStatus status);
}
