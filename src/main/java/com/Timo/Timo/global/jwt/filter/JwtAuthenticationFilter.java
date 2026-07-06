package com.Timo.Timo.global.jwt.filter;

import com.Timo.Timo.domain.user.repository.UserRepository;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.auth.service.BlackListService;
import com.Timo.Timo.global.auth.utils.TokenExtractor;
import com.Timo.Timo.global.jwt.provider.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;
  private final BlackListService blacklistService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {

    String token = TokenExtractor.resolveToken(request);

    if(token!=null
        && jwtTokenProvider.validateAccessToken(token)
        && !blacklistService.isBlackListed(token)){
      Long userId = jwtTokenProvider.getUserId(token);
      userRepository.findById(userId).ifPresent(user -> {
        CustomUserDetails userDetails = new CustomUserDetails(user, Map.of());
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
            );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      });
    }

    filterChain.doFilter(request, response);
  }
}
