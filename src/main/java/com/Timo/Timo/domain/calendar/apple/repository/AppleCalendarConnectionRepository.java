package com.Timo.Timo.domain.calendar.apple.repository;

import com.Timo.Timo.domain.calendar.apple.entity.AppleCalendarConnection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppleCalendarConnectionRepository extends JpaRepository<AppleCalendarConnection, Long> {

  Optional<AppleCalendarConnection> findByUserId(Long userId);
  boolean existsByUserId(Long userId);
}
