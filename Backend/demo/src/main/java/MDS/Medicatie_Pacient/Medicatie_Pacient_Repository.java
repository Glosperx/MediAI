package MDS.Medicatie_Pacient;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface Medicatie_Pacient_Repository extends JpaRepository<Medicatie_Pacient, Long> {
    List<Medicatie_Pacient> findByPacientUserId(Long pacientId);
    List<Medicatie_Pacient> findByDoctorUserId(Long doctorId);
    void deleteByPacientUserId(Long pacientId);
    void deleteByDoctorUserId(Long doctorId);
}