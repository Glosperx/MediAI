package MDS.Diagnostic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Diagnostic_Repository extends JpaRepository<Diagnostic, Long> {
    Optional<Diagnostic> findByNume(String nume);


}