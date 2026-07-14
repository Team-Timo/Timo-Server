package com.Timo.Timo.domain.calendar.repository;

import com.Timo.Timo.domain.calendar.entity.CalendarConnection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalendarConnectionRepository extends JpaRepository<CalendarConnection, Long> {

  Optional<CalendarConnection> findByUserId(Long userId);
  boolean existsByUserId(Long userId);
}
