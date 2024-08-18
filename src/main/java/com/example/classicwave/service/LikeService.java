package com.example.classicwave.service;

import com.example.classicwave.domain.Member;
import com.example.classicwave.error.CustomException;
import com.example.classicwave.error.ErrorCode;
import com.example.classicwave.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final static String BOOK_LIKE_KEY_PREFIX = "like:book:"; // 책을 관심 등록 한 유저들을 찾는 key
    private final static String MEMBER_LIKE_KEY_PREFIX = "like:member:"; // 유저가 관심 등록 한 책의 id list를 찾는 key
    private final RedisTemplate<String, String> redisTemplate;
    private final MemberRepository memberRepository;


    public void likeBook(Long bookId, Authentication authentication) {
        Member member = memberRepository.findByLogInId(authentication.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        String bookLikeKey = BOOK_LIKE_KEY_PREFIX + bookId;
        String memberLikeKey = MEMBER_LIKE_KEY_PREFIX + member.getId();

        redisTemplate.opsForSet().add(bookLikeKey, member.getId().toString());
        redisTemplate.opsForSet().add(memberLikeKey, bookId.toString());
    }

    public List<Long> getMemberLikeList(Authentication authentication) {
        Member member = memberRepository.findByLogInId(authentication.getName()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        String memberLikeKey = MEMBER_LIKE_KEY_PREFIX + member.getId();
        Set<String> likedBooks = redisTemplate.opsForSet().members(memberLikeKey);
        if (likedBooks == null) {
            return List.of();
        }
        return likedBooks.stream()
                .map(Long::parseLong)
                .toList();
    }

    public List<Long> getLikedMemberList(Long bookId) {
        String bookLikeKey = BOOK_LIKE_KEY_PREFIX + bookId;
        Set<String> members = redisTemplate.opsForSet().members(bookLikeKey);
        if (members == null) {
            return List.of();
        }
        return members.stream()
                .map(Long::parseLong)
                .toList();
    }

    public void unlikeBook(Long bookId, Authentication authentication) {
        Member member = memberRepository.findByLogInId(authentication.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        String bookLikeKey = BOOK_LIKE_KEY_PREFIX + bookId;
        String memberLikeKey = MEMBER_LIKE_KEY_PREFIX + member.getId();

        redisTemplate.opsForSet().remove(bookLikeKey, member.getId().toString());
        redisTemplate.opsForSet().remove(memberLikeKey, bookId.toString());
    }

}
