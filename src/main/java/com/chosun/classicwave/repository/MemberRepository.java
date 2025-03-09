package com.chosun.classicwave.repository;

import com.chosun.classicwave.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByLogInId(String logInId);
}
