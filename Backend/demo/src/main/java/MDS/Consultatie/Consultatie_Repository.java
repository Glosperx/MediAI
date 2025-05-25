package MDS.Consultatie;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface Consultatie_Repository extends JpaRepository<Consultatie, Long> {
    List<Consultatie> findByPacientUserId(Long pacientId);
    List<Consultatie> findByDoctorUserId(Long doctorId);
    void deleteByPacientUserId(Long pacientId);
    void deleteByDoctorUserId(Long doctorId);
}