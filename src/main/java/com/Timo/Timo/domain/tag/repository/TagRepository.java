package com.Timo.Timo.domain.tag.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Timo.Timo.domain.tag.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {

	@Query("""
		select count(t) > 0 from Tag t
		where t.name = :name
		  and (t.isDefault = true or t.user.id = :userId)
		""")
	boolean existsAccessibleByName(
			@Param("userId") Long userId,
			@Param("name") String name
	);

	@Query("""
		select t from Tag t
		where t.isDefault = true or t.user.id = :userId
		order by t.isDefault desc, t.id asc
		""")
	List<Tag> findAccessibleTags(@Param("userId") Long userId);
}
