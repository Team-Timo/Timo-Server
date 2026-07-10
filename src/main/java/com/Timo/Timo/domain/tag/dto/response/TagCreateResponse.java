package com.Timo.Timo.domain.tag.dto.response;

import com.Timo.Timo.domain.tag.entity.Tag;
import com.fasterxml.jackson.annotation.JsonProperty;

public record TagCreateResponse(
		Long tagId,
		String name,
		@JsonProperty("isDefault")
		boolean isDefault
) {

	public static TagCreateResponse from(Tag tag) {
		return new TagCreateResponse(tag.getId(), tag.getName(), tag.isDefault());
	}
}
