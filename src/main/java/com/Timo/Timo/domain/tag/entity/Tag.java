package com.Timo.Timo.domain.tag.entity;

import com.Timo.Timo.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tags",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"user_id", "name"})
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(name = "name", nullable = false, length = 20)
	private String name;

	@Column(name = "is_default", nullable = false)
	private boolean isDefault;

	private Tag(User user, String name, boolean isDefault) {
		this.user = user;
		this.name = name;
		this.isDefault = isDefault;
	}

	public static Tag ofUser(User user, String name) {
		return new Tag(user, name, false);
	}
}
