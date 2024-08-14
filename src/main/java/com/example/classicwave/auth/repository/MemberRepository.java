package com.example.classicwave.auth.repository;

import com.example.classicwave.auth.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByLogInId(String logInId);
}
