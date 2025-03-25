package com.example.upload.global.security;

import com.example.upload.domain.member.member.entity.Member;
import com.example.upload.domain.member.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private final MemberService memberService;

    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String oauthId = oAuth2User.getName(); //식별자
        String providerType = userRequest.getClientRegistration().getRegistrationId();


        Map<String, Object> attributes = oAuth2User.getAttributes();
        Map<String, Object> propeties = (Map<String, Object>)attributes.get("properties");

        String nickname = (String)propeties.get("nickname");
        String profileImage = (String)propeties.get("profile_image");
        String username = providerType + "__" + oauthId;

        Optional<Member> opMember = memberService.findByUsername(username);

        if(opMember.isPresent()) {
            Member member = opMember.get();
            member.update(nickname);

            return new SecurityUser(member);
        }

        Member member = memberService.join(username, "", nickname, profileImage);

        return new SecurityUser(member);
    }

}
