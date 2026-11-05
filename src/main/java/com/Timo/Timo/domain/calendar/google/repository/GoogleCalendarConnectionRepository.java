package com.Timo.Timo.domain.calendar.google.repository;

import com.Timo.Timo.domain.calendar.google.entity.GoogleCalendarConnection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GoogleCalendarConnectionRepository extends JpaRepository<GoogleCalendarConnection, Long> {

  Optional<GoogleCalendarConnection> findByUserId(Long userId);
  boolean existsByUserId(Long userId);

  @Query("""
		select coalesce(c.refreshToken, c.accessToken)
		from GoogleCalendarConnection c
		where c.user.id = :userId
		""")
  Optional<String> findTokenToRevokeByUserId(@Param("userId") Long userId);
}
