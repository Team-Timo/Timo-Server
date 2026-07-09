package com.Timo.Timo.domain.terms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Timo.Timo.domain.terms.docs.TermsControllerDocs;
import com.Timo.Timo.domain.terms.dto.response.TermsListResponse;
import com.Timo.Timo.domain.terms.exception.TermsSuccessCode;
import com.Timo.Timo.domain.terms.service.TermsService;
import com.Timo.Timo.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
@Tag(name = "Terms", description = "약관 API")
public class TermsController implements TermsControllerDocs {

	private final TermsService termsService;

	@Override
	@GetMapping
	public ResponseEntity<BaseResponse<TermsListResponse>> getTerms(
		@RequestParam(required = false) String type
	) {
		TermsListResponse response = termsService.getTerms(type);

		return ResponseEntity.ok(
			BaseResponse.onSuccess(TermsSuccessCode.TERMS_RETRIEVED, response)
		);
	}
}