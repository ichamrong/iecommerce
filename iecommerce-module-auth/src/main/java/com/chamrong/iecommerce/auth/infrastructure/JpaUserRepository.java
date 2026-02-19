package com.chamrong.iecommerce.auth.infrastructure;

import com.chamrong.iecommerce.auth.domain.User;
import com.chamrong.iecommerce.auth.domain.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaUserRepository implements UserRepository {

  private final UserJpaInterface jpaInterface;

  public JpaUserRepository(UserJpaInterface jpaInterface) {
    this.jpaInterface = jpaInterface;
  }

  @Override
  public Optional<User> findById(Long id) {
    return jpaInterface.findById(id);
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return jpaInterface.findByUsername(username);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return jpaInterface.findByEmail(email);
  }

  @Override
  public User save(User user) {
    return jpaInterface.save(user);
  }

  @Override
  public List<User> findAll() {
    return jpaInterface.findAll();
  }

  public interface UserJpaInterface extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);
  }
}
