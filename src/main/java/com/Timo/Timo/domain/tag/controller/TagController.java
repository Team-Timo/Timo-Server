package com.Timo.Timo.domain.tag.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Timo.Timo.domain.tag.docs.TagControllerDocs;
import com.Timo.Timo.domain.tag.dto.request.TagCreateRequest;
import com.Timo.Timo.domain.tag.dto.response.TagCreateResponse;
import com.Timo.Timo.domain.tag.exception.TagSuccessCode;
import com.Timo.Timo.domain.tag.service.TagService;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@Tag(name = "Tag", description = "태그 API")
public class TagController implements TagControllerDocs {

	private final TagService tagService;

	@Override
	@PostMapping
	public ResponseEntity<BaseResponse<TagCreateResponse>> createTag(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@Valid @RequestBody TagCreateRequest request
	) {
		TagCreateResponse response = tagService.createTag(userDetails.getUserId(), request);

		return ResponseEntity
				.status(TagSuccessCode.CREATED.getHttpStatus())
				.body(BaseResponse.onSuccess(TagSuccessCode.CREATED, response));
	}

	@Override
	@DeleteMapping("/{tagId}")
	public ResponseEntity<BaseResponse<Void>> deleteTag(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable Long tagId
	) {
		tagService.deleteTag(userDetails.getUserId(), tagId);

		return ResponseEntity
				.status(TagSuccessCode.DELETED.getHttpStatus())
				.body(BaseResponse.onSuccess(TagSuccessCode.DELETED, null));
	}
}
