package MDS.Diagnostic_Pacient;

import MDS.Users.User;
import MDS.Diagnostic.Diagnostic;
import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "Diagnostic_Pacient")
public class Diagnostic_Pacient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ID_Pacient", nullable = false) // Aceasta face referire la USER_ID din Users
    private User pacient;

    @Column(name = "Probabilitate")
    private Double probabilitate;

    @ManyToOne
    @JoinColumn(name = "ID_Diagnostic", nullable = false)
    private Diagnostic diagnostic;

    @Column(name = "Data_Diagnostic", nullable = false)
    private Timestamp dataDiagnostic;

    @ManyToOne
    @JoinColumn(name = "ID_Doctor") // Aceasta face referire la USER_ID din Users (pentru doctor)
    private User doctor;

    // Constructori
    public Diagnostic_Pacient() {}

    public Diagnostic_Pacient(User pacient, Diagnostic diagnostic, Timestamp dataDiagnostic, User doctor, double probabilitate) {
        this.pacient = pacient;
        this.diagnostic = diagnostic;
        this.dataDiagnostic = dataDiagnostic;
        this.probabilitate = probabilitate;
        this.doctor = doctor;
    }

    // Getters și Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getPacient() { return pacient; }
    public void setPacient(User pacient) { this.pacient = pacient; }
    public Diagnostic getDiagnostic() { return diagnostic; }
    public void setDiagnostic(Diagnostic diagnostic) { this.diagnostic = diagnostic; }
    public Timestamp getDataDiagnostic() { return dataDiagnostic; }
    public void setDataDiagnostic(Timestamp dataDiagnostic) { this.dataDiagnostic = dataDiagnostic; }
    public User getDoctor() { return doctor; }
    public void setDoctor(User doctor) { this.doctor = doctor; }

    public Double getProbabilitate() {
        return probabilitate;
    }

    public void setProbabilitate(Double probabilitate) {
        this.probabilitate = probabilitate;
    }
}