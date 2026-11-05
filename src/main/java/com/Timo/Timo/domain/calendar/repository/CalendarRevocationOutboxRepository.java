package com.Timo.Timo.domain.calendar.repository;

import com.Timo.Timo.domain.calendar.entity.CalendarRevocationOutbox;
import com.Timo.Timo.domain.calendar.enums.RevocationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalendarRevocationOutboxRepository extends
    JpaRepository<CalendarRevocationOutbox, Long> {

  List<CalendarRevocationOutbox> findTop50ByStatus(RevocationStatus status);
}
