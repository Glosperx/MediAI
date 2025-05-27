package MDS.Simptome_Pacient;

import MDS.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface Simptom_Pacient_Repository extends JpaRepository<Simptom_Pacient, Long> {
    List<Simptom_Pacient> findByPacientUserId(Long pacientId);
    List<Simptom_Pacient> findByPacientAndDataRaportareGreaterThanEqual(User pacient, Timestamp data);
    void deleteByPacientUserId(Long pacientId);
}

