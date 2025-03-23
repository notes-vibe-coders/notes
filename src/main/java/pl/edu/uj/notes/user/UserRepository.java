package pl.edu.uj.notes.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface UserRepository extends JpaRepository<UserEntity, Integer> {
  boolean existsByUsername(String username);
}
