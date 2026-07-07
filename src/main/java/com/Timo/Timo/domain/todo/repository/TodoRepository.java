package com.Timo.Timo.domain.todo.repository;

import com.Timo.Timo.domain.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, Long> {
}
