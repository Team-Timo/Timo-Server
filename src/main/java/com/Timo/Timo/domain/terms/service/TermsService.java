package com.Timo.Timo.domain.terms.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Timo.Timo.domain.terms.dto.response.TermsDetailResponse;
import com.Timo.Timo.domain.terms.entity.Terms;
import com.Timo.Timo.domain.terms.enums.TermsLanguage;
import com.Timo.Timo.domain.terms.enums.TermsType;
import com.Timo.Timo.domain.terms.repository.TermsRepository;
import com.Timo.Timo.global.exception.CustomException;
import com.Timo.Timo.global.exception.code.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsService {
	private final TermsRepository termsRepository;

	public TermsDetailResponse getTermsByCondition(String type, String language) {
		Terms terms = termsRepository.findFirstByTypeAndLanguageOrderByIdDesc(
				TermsType.from(type),
				TermsLanguage.from(language)
			)
			.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

		return new TermsDetailResponse(
			terms.getId(),
			terms.getType().name(),
			terms.getLanguage().name(),
			terms.getVersion(),
			terms.getTitle(),
			terms.getContent()
		);
	}
}