package com.example.upload.global.security;

import com.example.upload.domain.member.member.entity.Member;
import com.example.upload.domain.member.member.service.MemberService;
import com.example.upload.global.Rq;
import com.example.upload.global.app.AppConfig
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
class CustomAuthenticationSuccessHandler(
    private val rq: Rq,
    private val memberService: MemberService
): AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(request: HttpServletRequest, response: HttpServletResponse, authentication: Authentication) {
        val session: HttpSession = request.session;

        var redirectUrl:String = session.getAttribute("redirectUrl") as String

        redirectUrl.let {
            redirectUrl = AppConfig.getSiteFrontUrl()
        }

        session.removeAttribute("redirectUrl")

        val actor:Member = rq.getRealActor(rq.actor);
        val accessToken = memberService.genAccessToken(actor);

        rq.addCookie("accessToken", accessToken);
        rq.addCookie("apiKey", actor.apiKey);

        response.sendRedirect(redirectUrl);
    }
}
