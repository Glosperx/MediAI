package MDS.Diagnostic_Pacient;

import MDS.Diagnostic.Diagnostic;
import MDS.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface Diagnostic_Pacient_Repository extends JpaRepository<Diagnostic_Pacient, Long> {

    List<Diagnostic_Pacient> findByPacientUserId(Long pacientId);
    List<Diagnostic_Pacient> findByDoctorUserId(Long doctorId);
    List<Diagnostic_Pacient> findByPacientAndDataDiagnosticGreaterThanEqual(User pacient, Timestamp data);

    void deleteByPacientUserId(Long pacientId);
    void deleteByDoctorUserId(Long doctorId);
    void deleteByDiagnostic(Diagnostic diagnostic);
}