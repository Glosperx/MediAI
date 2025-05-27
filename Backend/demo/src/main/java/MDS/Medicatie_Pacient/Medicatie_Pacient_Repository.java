package MDS.Medicatie_Pacient;

import MDS.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;

public interface Medicatie_Pacient_Repository extends JpaRepository<Medicatie_Pacient, Long> {
    List<Medicatie_Pacient> findByPacientUserId(Long pacientId);
    List<Medicatie_Pacient> findByDoctorUserId(Long doctorId);
    List<Medicatie_Pacient> findByPacientAndDataPrescriereGreaterThanEqual(User pacient, Timestamp data);
    void deleteByPacientUserId(Long pacientId);
    void deleteByDoctorUserId(Long doctorId);
}