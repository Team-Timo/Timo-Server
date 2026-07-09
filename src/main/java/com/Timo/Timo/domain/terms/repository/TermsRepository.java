package com.Timo.Timo.domain.terms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Timo.Timo.domain.terms.entity.Terms;
import com.Timo.Timo.domain.terms.enums.TermsType;

public interface TermsRepository extends JpaRepository<Terms, Long> {
	List<Terms> findAllByOrderByIdAsc();
	List<Terms> findAllByTypeOrderByIdAsc(TermsType type);
}