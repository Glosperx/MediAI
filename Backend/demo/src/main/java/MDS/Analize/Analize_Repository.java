package MDS.Analize;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Analize_Repository extends JpaRepository<Analize, Long> {
    Optional<Analize> findByTipAnaliza(String tipAnaliza);
}