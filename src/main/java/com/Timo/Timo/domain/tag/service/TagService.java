package com.Timo.Timo.domain.tag.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Timo.Timo.domain.tag.dto.request.TagCreateRequest;
import com.Timo.Timo.domain.tag.dto.response.TagCreateResponse;
import com.Timo.Timo.domain.tag.dto.response.TagListResponse;
import com.Timo.Timo.domain.tag.entity.Tag;
import com.Timo.Timo.domain.tag.exception.TagErrorCode;
import com.Timo.Timo.domain.tag.repository.TagRepository;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.exception.UserErrorCode;
import com.Timo.Timo.domain.user.repository.UserRepository;
import com.Timo.Timo.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TagService {

	private final TagRepository tagRepository;
	private final UserRepository userRepository;

	public TagCreateResponse createTag(Long userId, TagCreateRequest request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

		if (tagRepository.existsAccessibleByName(userId, request.name())) {
			throw new CustomException(TagErrorCode.DUPLICATE_TAG_NAME);
		}

		try {
			Tag tag = tagRepository.saveAndFlush(Tag.ofUser(user, request.name()));
			return TagCreateResponse.from(tag);
		} catch (DataIntegrityViolationException exception) {
			throw new CustomException(TagErrorCode.DUPLICATE_TAG_NAME);
		}
	}

	@Transactional(readOnly = true)
	public TagListResponse getTags(Long userId) {
		return TagListResponse.from(tagRepository.findAccessibleTags(userId));
	}

	public void deleteTag(Long userId, Long tagId) {
		if (tagId == null || tagId <= 0) {
			throw new CustomException(TagErrorCode.INVALID_TAG_ID);
		}

		Tag tag = tagRepository.findById(tagId)
				.orElseThrow(() -> new CustomException(TagErrorCode.TAG_NOT_FOUND));

		if (tag.isDefault()) {
			throw new CustomException(TagErrorCode.TAG_DELETE_FORBIDDEN);
		}

		if (!isOwnedBy(tag, userId)) {
			throw new CustomException(TagErrorCode.TAG_NOT_FOUND);
		}

		tagRepository.delete(tag);
	}

	private boolean isOwnedBy(Tag tag, Long userId) {
		return tag.getUser() != null && tag.getUser().getId().equals(userId);
	}
}
