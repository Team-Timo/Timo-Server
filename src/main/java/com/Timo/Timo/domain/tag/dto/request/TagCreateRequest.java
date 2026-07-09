package com.Timo.Timo.domain.tag.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagCreateRequest(
		@NotBlank(message = "태그 이름은 필수입니다.")
		@Size(max = 20, message = "태그 이름은 20자를 초과할 수 없습니다.")
		String name
) { }
