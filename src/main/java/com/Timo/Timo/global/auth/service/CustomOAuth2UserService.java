package com.Timo.Timo.global.auth.service;

import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.repository.UserRepository;
import com.Timo.Timo.global.auth.dto.CustomUserDetails;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final UserRepository userRepository;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
    OAuth2User oAuth2User = delegate.loadUser(userRequest);

    Map<String, Object> attributes = oAuth2User.getAttributes();
    String email = (String) attributes.get("email");
    String name = (String) attributes.get("name");
    String picture = (String) attributes.get("picture");

    User user = userRepository.findByEmail(email)
        .map(existing -> {
          existing.update(name, picture);
          return existing;
        })
        .orElseGet(() -> userRepository.save(
            User.builder()
                .email(email)
                .name(name)
                .picture(picture)
                .provider(User.Provider.GOOGLE)
                .build()
        ));

    // 우리 서비스용 객체로 변환
    return new CustomUserDetails(user, attributes);
  }
}
