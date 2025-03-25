package com.example.upload.domain.member.member.service;

import com.example.upload.domain.member.member.entity.Member;
import com.example.upload.standard.util.Ut;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

    @Value("${custom.jwt.secret-key}")
    private String keyString;

    @Value("${custom.jwt.expire-seconds}")
    private int expireSeconds;

    String genAccessToken(Member member) {

        return Ut.jwt.createToken(
                keyString,
                expireSeconds,
                Map.of("id", member.getId(), "username", member.getUsername(), "nickname", member.getNickname())
        );
    }

    Map<String, Object> getPayload(String token) {

        if(!Ut.jwt.isValidToken(keyString, token)) return null;

        Map<String, Object> payload = Ut.jwt.getPayload(keyString, token);
        Number idNo = (Number)payload.get("id");
        long id = idNo.longValue();
        String username = (String)payload.get("username");
        String nickname = (String)payload.get("nickname");

        return Map.of("id", id, "username", username, "nickname", nickname);
    }
}
