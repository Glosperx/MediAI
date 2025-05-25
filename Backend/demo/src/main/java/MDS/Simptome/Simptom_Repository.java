package MDS.Simptome;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Simptom_Repository extends JpaRepository<Simptom, Long> {
    Optional<Simptom> findByNume(String nume);
}