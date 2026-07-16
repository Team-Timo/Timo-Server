package com.Timo.Timo.domain.calendar.repository;

import com.Timo.Timo.domain.calendar.entity.CalendarConnection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CalendarConnectionRepository extends JpaRepository<CalendarConnection, Long> {

  Optional<CalendarConnection> findByUserId(Long userId);
  boolean existsByUserId(Long userId);

  @Query("""
		select coalesce(c.refreshToken, c.accessToken)
		from CalendarConnection c
		where c.user.id = :userId
		""")
  Optional<String> findTokenToRevokeByUserId(@Param("userId") Long userId);
}
