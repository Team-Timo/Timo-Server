package com.Timo.Timo.domain.tag.dto.response;

import com.Timo.Timo.domain.tag.entity.Tag;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

public record TagCreateResponse(
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		Long tagId,
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		String name,
		@JsonProperty("isDefault")
		@Schema(requiredMode = Schema.RequiredMode.REQUIRED)
		boolean isDefault
) {

	public static TagCreateResponse from(Tag tag) {
		return new TagCreateResponse(tag.getId(), tag.getName(), tag.isDefault());
	}
}
