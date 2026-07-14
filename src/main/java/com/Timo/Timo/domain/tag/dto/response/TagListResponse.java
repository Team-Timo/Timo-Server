package com.Timo.Timo.domain.tag.dto.response;

import java.util.List;

import com.Timo.Timo.domain.tag.entity.Tag;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

public record TagListResponse(
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		List<TagResponse> tags
) {
	public static TagListResponse from(List<Tag> tags) {
		return new TagListResponse(tags.stream()
				.map(TagResponse::from)
				.toList());
	}

	public record TagResponse(
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			Long tagId,
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			String name,
			@JsonProperty("isDefault")
			@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
			boolean isDefault
	) {
		public static TagResponse from(Tag tag) {
			return new TagResponse(tag.getId(), tag.getName(), tag.isDefault());
		}
	}
}
