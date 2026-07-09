package com.Timo.Timo.domain.tag.dto.response;

import java.util.List;

import com.Timo.Timo.domain.tag.entity.Tag;
import com.fasterxml.jackson.annotation.JsonProperty;

public record TagListResponse(
		List<TagResponse> tags
) {
	public static TagListResponse from(List<Tag> tags) {
		return new TagListResponse(tags.stream()
				.map(TagResponse::from)
				.toList());
	}

	public record TagResponse(
			Long tagId,
			String name,
			@JsonProperty("isDefault")
			boolean isDefault
	) {
		public static TagResponse from(Tag tag) {
			return new TagResponse(tag.getId(), tag.getName(), tag.isDefault());
		}
	}
}
