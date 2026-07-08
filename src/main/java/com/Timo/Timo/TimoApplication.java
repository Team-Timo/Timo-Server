package com.Timo.Timo;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableJpaAuditing
public class TimoApplication {

	/**
	 * 서버가 어느 지역에 배포되든 감사 로그/타임스탬프가 항상 UTC 기준으로 기록되도록
	 * JVM 기본 시간대를 UTC로 고정한다. 사용자별 시간대 변환은 각 조회 시점에 처리한다.
	 */
	@PostConstruct
	void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	public static void main(String[] args) {
		SpringApplication.run(TimoApplication.class, args);
	}

}
