package com.Timo.Timo.domain.tag.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Timo.Timo.domain.tag.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
}
