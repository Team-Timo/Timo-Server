package com.Timo.Timo.domain.user.controller;

import com.Timo.Timo.domain.user.docs.UserLanguageDocs;
import com.Timo.Timo.domain.user.docs.UserProfileDocs;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Timo.Timo.domain.user.dto.response.UserProfileResponse;
import com.Timo.Timo.domain.user.exception.UserSuccessCode;
import com.Timo.Timo.domain.user.service.UserService;
import com.Timo.Timo.global.exception.CustomException;
import com.Timo.Timo.global.exception.code.ErrorCode;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 API")
public class UserController implements UserProfileDocs, UserLanguageDocs {

	private final UserService userService;

	@Override
	@GetMapping
	public ResponseEntity<BaseResponse<UserProfileResponse>> getMyProfile(
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		Long userId = userDetails.getUserId();
		UserProfileResponse response = userService.getMyProfile(userId);

		return ResponseEntity.ok(
			BaseResponse.onSuccess(UserSuccessCode.PROFILE_RETRIEVED, response)
		);
	}
}
