package com.Timo.Timo.domain.terms.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Timo.Timo.domain.terms.dto.response.TermsListResponse;
import com.Timo.Timo.domain.terms.dto.response.TermsListResponse.TermsResponse;
import com.Timo.Timo.domain.terms.entity.Terms;
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

	public TermsListResponse getTerms(String type) {
		List<Terms> terms = type == null
			? termsRepository.findAllByOrderByIdAsc()
			: termsRepository.findAllByTypeOrderByIdAsc(parseType(type));

		return new TermsListResponse(
			terms.stream()
				.map(this::toResponse)
				.toList()
		);
	}

	private TermsType parseType(String type) {
		if (type.isBlank()) {
			throw new CustomException(ErrorCode.BAD_REQUEST);
		}
		return TermsType.from(type);
	}

	private TermsResponse toResponse(Terms terms) {
		return new TermsResponse(
			terms.getId(),
			terms.getType().name(),
			terms.getTitle(),
			terms.getContent()
		);
	}
}