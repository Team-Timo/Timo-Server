package com.Timo.Timo.domain.home.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Timo.Timo.domain.home.docs.HomeControllerDocs;
import com.Timo.Timo.domain.home.dto.response.HomeResponse;
import com.Timo.Timo.domain.home.exception.HomeSuccessCode;
import com.Timo.Timo.domain.home.service.HomeService;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
@Tag(name = "Home", description = "HOME API")
public class HomeController implements HomeControllerDocs {

	private final HomeService homeService;

	@Override
	@GetMapping
	public ResponseEntity<BaseResponse<HomeResponse>> getHome(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam(required = false) String filter,
			@RequestParam(required = false) String baseDate
	) {
		HomeResponse response = homeService.getHome(userDetails.getUserId(), filter, baseDate);

		return ResponseEntity
				.status(HomeSuccessCode.GET_HOME.getHttpStatus())
				.body(BaseResponse.onSuccess(HomeSuccessCode.GET_HOME, response));
	}
}
