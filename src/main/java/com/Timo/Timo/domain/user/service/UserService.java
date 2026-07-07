package com.Timo.Timo.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Timo.Timo.domain.user.dto.request.UpdateLanguageRequest;
import com.Timo.Timo.domain.user.dto.response.UserProfileResponse;
import com.Timo.Timo.domain.user.dto.response.UpdateLanguageResponse;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.exception.UserErrorCode;
import com.Timo.Timo.domain.user.repository.UserRepository;
import com.Timo.Timo.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;

	public UserProfileResponse getMyProfile(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

		return UserProfileResponse.from(user);
	}

	@Transactional
	public UpdateLanguageResponse updateLanguage(Long userId, UpdateLanguageRequest request) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

		user.updateLanguage(request.language());

		return new UpdateLanguageResponse(user.getLanguage());
	}
}
