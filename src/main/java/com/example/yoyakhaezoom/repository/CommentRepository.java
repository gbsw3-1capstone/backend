package com.example.yoyakhaezoom.repository;

import com.example.yoyakhaezoom.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}