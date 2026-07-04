package com.Timo.Timo.global.auth.service;

import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.enums.Provider;
import com.Timo.Timo.domain.user.repository.UserRepository;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.exception.code.ErrorCode;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final UserRepository userRepository;

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
    OAuth2User oAuth2User = delegate.loadUser(userRequest);

    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    Provider provider = Provider.valueOf(registrationId.toUpperCase());

    Map<String, Object> attributes = oAuth2User.getAttributes();
    String name = (String) attributes.get("name");
    String email = (String) attributes.get("email");
    String imageUrl = (String) attributes.get("picture");
    String providerId = (String) attributes.get("sub");

    if (!StringUtils.hasText(email)) {
      throw new OAuth2AuthenticationException(
          new OAuth2Error(ErrorCode.OAUTH2_INVALID_USER_INFO.getCode()),
          ErrorCode.OAUTH2_INVALID_USER_INFO.getMessage()
      );
    }

    User user;
    try {
      user = userRepository.findByProviderAndProviderId(provider, providerId)
          .map(existing -> {
            existing.update(name, imageUrl);
            return existing;
          })
          .orElseGet(() -> userRepository.save(
              User.builder()
                  .name(name)
                  .email(email)
                  .profileImageUrl(imageUrl)
                  .provider(provider)
                  .providerId(providerId)
                  .build()
          ));
    } catch (DataIntegrityViolationException e) {
      user = userRepository.findByProviderAndProviderId(provider, providerId)
          .orElseThrow(() -> new OAuth2AuthenticationException(
              new OAuth2Error(ErrorCode.OAUTH2_INVALID_USER_INFO.getCode()),
              ErrorCode.OAUTH2_INVALID_USER_INFO.getMessage()
          ));
    }

    return new CustomUserDetails(user, attributes);
  }
}
