package com.Timo.Timo.domain.terms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Timo.Timo.domain.terms.entity.Terms;
import com.Timo.Timo.domain.terms.enums.TermsLanguage;
import com.Timo.Timo.domain.terms.enums.TermsType;

public interface TermsRepository extends JpaRepository<Terms, Long> {
	Optional<Terms> findFirstByTypeAndLanguageOrderByIdDesc(TermsType type, TermsLanguage language);
}