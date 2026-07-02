package com.Timo.Timo.domain.user.entity;

import com.Timo.Timo.domain.user.enums.Provider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  private String name;
  private String imageUrl;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Provider provider;

  @Builder
  private User(String email, String name, String imageUrl, Provider provider) {
    this.email = email;
    this.name = name;
    this.imageUrl = imageUrl;
    this.provider = provider;
  }

  public void update(String name, String imageUrl){
    this.name = name;
    this.imageUrl = imageUrl;
  }

}
