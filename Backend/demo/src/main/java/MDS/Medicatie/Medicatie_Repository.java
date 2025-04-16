package MDS.Medicatie;

import MDS.Diagnostic.Diagnostic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Medicatie_Repository extends JpaRepository<Medicatie, Long> {
    Optional<Medicatie> findByNume(String nume);
}