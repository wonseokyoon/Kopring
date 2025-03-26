package com.example.upload.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Component
class CustomAuthorizationRequestResolver(
    private val clientRegistrationRepository: ClientRegistrationRepository
): OAuth2AuthorizationRequestResolver {

    private var defaultResolver = DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization")

    override fun resolve(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        val authorizationRequest = defaultResolver.resolve(request) ?: return null
        return customizeAuthorizationRequest(request, authorizationRequest)
    }

    override fun resolve(request: HttpServletRequest, clientRegistrationId: String): OAuth2AuthorizationRequest? {
        val authorizationRequest = defaultResolver.resolve(request, clientRegistrationId) ?: return null
        return customizeAuthorizationRequest(request, authorizationRequest)
    }

    private fun customizeAuthorizationRequest(request:HttpServletRequest, authorizationRequest:OAuth2AuthorizationRequest):OAuth2AuthorizationRequest {
        if(authorizationRequest == null) {
            return OAuth2AuthorizationRequest.from(authorizationRequest)
                .build()
        }
        val customParam:String = request.getParameter("redirectUrl") // 클라이언트가 전달한 값

        if (customParam != null) {
            val session = request.session
            session.setAttribute("redirectUrl", customParam)
        }

        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .build();
    }
}
