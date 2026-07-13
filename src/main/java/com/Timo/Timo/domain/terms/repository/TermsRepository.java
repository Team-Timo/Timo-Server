package com.Timo.Timo.domain.terms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Timo.Timo.domain.terms.entity.Terms;

public interface TermsRepository extends JpaRepository<Terms, Long> {}