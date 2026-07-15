package com.Timo.Timo.global.auth.filter;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuthOriginCaptureFilter extends OncePerRequestFilter {

    public static final String SESSION_KEY = "OAUTH_FRONTEND_ORIGIN";
    private static final String AUTHORIZATION_PATH_PREFIX = "/oauth2/authorization/";
    private static final String ORIGIN_PARAM = "redirect_origin";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (request.getRequestURI().startsWith(AUTHORIZATION_PATH_PREFIX)) {
            String origin = request.getParameter(ORIGIN_PARAM);
            if (origin != null && !origin.isBlank()) {
                request.getSession().setAttribute(SESSION_KEY, origin);
            }
        }
        filterChain.doFilter(request, response);
    }
}
