package MDS.Simptome_Pacient;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Simptom_Pacient_Repository extends JpaRepository<Simptom_Pacient, Long> {
    List<Simptom_Pacient> findByPacientUserId(Long pacientId);
    void deleteByPacientUserId(Long pacientId);
}

