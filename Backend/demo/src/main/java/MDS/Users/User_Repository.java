package MDS.Users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface User_Repository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRol(String rol);
//    Optional<User> findByRol(String rol);
    boolean existsByEmail(String email);
    Optional<User> findFirstByRol(String rol);

}