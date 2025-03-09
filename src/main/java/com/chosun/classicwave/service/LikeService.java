package com.chosun.classicwave.service;

import com.chosun.classicwave.entity.Member;
import com.chosun.classicwave.error.CustomException;
import com.chosun.classicwave.error.ErrorCode;
import com.chosun.classicwave.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final static String BOOK_LIKE_KEY_PREFIX = "like:book:"; // 책을 관심 등록 한 유저들을 찾는 key
    private final static String MEMBER_LIKE_KEY_PREFIX = "like:member:"; // 유저가 관심 등록 한 책의 id list를 찾는 key
    private final static String SORTED_TOTAL_LIKES_KEY = "sorted:total_like";
    private final RedisTemplate<String, String> redisTemplate;
    private final MemberRepository memberRepository;

    public void likeBook(Long bookId, Authentication authentication) {
        Member member = memberRepository.findByLogInId(authentication.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        String bookLikeKey = BOOK_LIKE_KEY_PREFIX + bookId;
        String memberLikeKey = MEMBER_LIKE_KEY_PREFIX + member.getId();
        double now = Instant.now().toEpochMilli();

        redisTemplate.opsForSet().add(bookLikeKey, member.getId().toString());
        redisTemplate.opsForZSet().add(memberLikeKey, bookId.toString(), now);
        redisTemplate.opsForZSet().incrementScore(SORTED_TOTAL_LIKES_KEY, bookId.toString(), 1);
    }

    public List<Long> getMemberLikeList(Authentication authentication) {
        Member member = memberRepository.findByLogInId(authentication.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));


        String memberLikeKey = MEMBER_LIKE_KEY_PREFIX + member.getId();

        Set<String> likedBooks = redisTemplate.opsForZSet().range(memberLikeKey, 0, -1);
        if (likedBooks == null) {
            return List.of();
        }
        return likedBooks.stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    public List<Long> getLikedMemberList(Long bookId) {
        String bookLikeKey = BOOK_LIKE_KEY_PREFIX + bookId;
        Set<String> members = redisTemplate.opsForZSet().range(bookLikeKey, 0, -1);
        if (members == null) {
            return List.of();
        }
        return members.stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    public void unlikeBook(Long bookId, Authentication authentication) {
        Member member = memberRepository.findByLogInId(authentication.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        String bookLikeKey = BOOK_LIKE_KEY_PREFIX + bookId;
        String memberLikeKey = MEMBER_LIKE_KEY_PREFIX + member.getId();

        redisTemplate.opsForSet().remove(bookLikeKey, member.getId().toString());
        redisTemplate.opsForZSet().remove(memberLikeKey, bookId.toString());
        redisTemplate.opsForZSet().incrementScore(SORTED_TOTAL_LIKES_KEY, bookId.toString(), -1);
    }
}
