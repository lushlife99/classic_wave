package com.example.classicwave.auth.repository;

import com.example.classicwave.auth.domain.Likes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikesRepository extends JpaRepository<Likes, Long> {
}
