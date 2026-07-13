package com.Timo.Timo.domain.terms.entity;

import com.Timo.Timo.domain.terms.enums.TermsLanguage;
import com.Timo.Timo.domain.terms.enums.TermsType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "terms",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"type", "language", "version"})
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Terms {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 20)
	private TermsType type;

	@Enumerated(EnumType.STRING)
	@Column(name = "language", nullable = false, length = 2)
	private TermsLanguage language;

	@Column(name = "version", nullable = false, length = 20)
	private String version;

	@Column(name = "title", nullable = false, length = 100)
	private String title;

	@Column(name = "content", nullable = false, columnDefinition = "TEXT")
	private String content;
}