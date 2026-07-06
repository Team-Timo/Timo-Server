package com.Timo.Timo.domain.user.repository;

import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.enums.Provider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByProviderAndProviderId(Provider provider, String providerId);
}
