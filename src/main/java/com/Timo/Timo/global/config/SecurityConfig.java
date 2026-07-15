package com.Timo.Timo.global.config;

import com.Timo.Timo.global.auth.filter.OriginValidationFilter;
import com.Timo.Timo.global.auth.handler.JwtAuthenticationEntryPoint;
import com.Timo.Timo.global.auth.handler.OAuthFailureHandler;
import com.Timo.Timo.global.auth.filter.OAuthOriginCaptureFilter;
import com.Timo.Timo.global.auth.handler.OAuthSuccessHandler;
import com.Timo.Timo.global.auth.service.CustomOAuth2UserService;
import com.Timo.Timo.global.jwt.filter.JwtAuthenticationFilter;
import com.Timo.Timo.global.logging.MdcLoggingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomOAuth2UserService customOAuth2UserService;
  private final OAuthSuccessHandler oAuthSuccessHandler;
  private final OAuthFailureHandler oAuthFailureHandler;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final CorsConfigurationSource corsConfigurationSource;
  private final OAuthOriginCaptureFilter oAuthOriginCaptureFilter;
  private final OriginValidationFilter originValidationFilter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
      .cors(cors -> cors.configurationSource(corsConfigurationSource))
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers(
          "/",
					"/swagger-ui/**",
					"/swagger-ui.html",
					"/v3/api-docs/**",
          "/login/**",
          "/oauth2/**",
          "/api/v1/auth/reissue",
          "/api/v1/auth/token",
          "/api/v1/terms",
          "/actuator/health",
                        "/actuator/prometheus"
				).permitAll()
				.anyRequest().authenticated())
        .oauth2Login(oauth2 -> oauth2
            .userInfoEndpoint(userInfo ->
                userInfo.userService(customOAuth2UserService)
            )
            .successHandler(oAuthSuccessHandler)
            .failureHandler(oAuthFailureHandler)
        )
        .exceptionHandling(exception ->
            exception.authenticationEntryPoint(jwtAuthenticationEntryPoint)
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(mdcLoggingFilter(), JwtAuthenticationFilter.class)
        .addFilterBefore(oAuthOriginCaptureFilter, OAuth2AuthorizationRequestRedirectFilter.class)
        .addFilterBefore(originValidationFilter, JwtAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public MdcLoggingFilter mdcLoggingFilter() {
    return new MdcLoggingFilter();
  }

  @Bean
  public FilterRegistrationBean<MdcLoggingFilter> mdcLoggingFilterRegistration(
      MdcLoggingFilter mdcLoggingFilter
  ) {
    FilterRegistrationBean<MdcLoggingFilter> registrationBean = new FilterRegistrationBean<>(mdcLoggingFilter);
    registrationBean.setEnabled(false);
    return registrationBean;
  }

  @Bean
  public FilterRegistrationBean<OriginValidationFilter> originValidationFilterRegistration(
      OriginValidationFilter originValidationFilter
  ) {
    FilterRegistrationBean<OriginValidationFilter> registrationBean =
        new FilterRegistrationBean<>(originValidationFilter);
    registrationBean.setEnabled(false);
    return registrationBean;
  }
}
