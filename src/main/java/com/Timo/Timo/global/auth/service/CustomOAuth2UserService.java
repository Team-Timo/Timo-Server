package com.Timo.Timo.global.auth.service;

import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.repository.UserRepository;
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
    // 기본 구글 유저 정보 가져오기
    OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
    OAuth2User oAuth2User = delegate.loadUser(userRequest);

    // 구글이 준 원본 JSON 데이터 꺼내기
    Map<String, Object> attributes = oAuth2User.getAttributes();
    String email = (String) attributes.get("email");
    String name = (String) attributes.get("name");
    String picture = (String) attributes.get("picture");

    // DB에서 유저 찾기 + 없으면 생성
    User user = userRepository.findByEmail(email)
        .map(existing -> {
          // 이름/사진 바뀌었을 수도 있으므로 최신 정보로 업데이트
          existing.update(name, picture);
          return existing;
        })
        // 없는 유저의 경우 첫 로그인 -> 자동 회원가입
        .orElseGet(() -> userRepository.save(
            User.builder()
                .email(email)
                .name(name)
                .picture(picture)
                .provider(User.Provider.GOOGLE)
                .build()
        ));

    // 우리 서비스용 객체로 변환
    return new CustomerUserDetails(user, attributes);
  }
}
