package com.Timo.Timo.domain.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  private String name;
  private String picture;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Provider provider;

  // TODO : 변경 가능한 건지 기획에게 확인 필요 (피그마에 관련 의견 남겨둠)
  public void update(String name, String picture){
    this.name = name;
    this.picture = picture;
  }

  public enum Provider { GOOGLE }
}
