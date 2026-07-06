package com.Timo.Timo.global.auth.principal;

import com.Timo.Timo.domain.user.entity.User;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Getter
public class CustomUserDetails implements OAuth2User, UserDetails {

  private final User user;
  private final Map<String, Object> attributes;

  public CustomUserDetails(User user, Map<String, Object> attributes){
    this.user = user;
    this.attributes = attributes;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public String getName() {
    return user.getEmail();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_USER"));
  }

  @Override
  public String getPassword(){
    return null;
  }

  @Override
  public String getUsername(){
    return user.getEmail();
  }
}
