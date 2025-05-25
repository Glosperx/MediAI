package MDS.Consultatie;

import MDS.Users.User;
import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "Consultatie")
public class Consultatie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CONSULTATIE")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ID_Pacient", nullable = false)
    private User pacient;

    @ManyToOne
    @JoinColumn(name = "ID_Doctor", nullable = false)
    private User doctor;

    @Column(name = "Nota")
    private String nota;

    @Column(name = "Aprobat", nullable = false)
//    private Integer aprobat;
    private boolean aprobat = false;

    @Column(name = "Data_consultatie", nullable = false)
    private Timestamp dataConsultatie;

    // Constructors
    public Consultatie() {}


    public Consultatie(User pacient, User doctor, String consultatieNota, boolean aprobat, java.sql.Timestamp currentTime) {
        this.pacient = pacient;
        this.doctor = doctor;
        this.nota = consultatieNota;
        this.aprobat = aprobat;
        this.dataConsultatie = currentTime;
    }

    public Consultatie(User pacient, User doctor, String consultatieNota, boolean aprobat, java.security.Timestamp currentTime) {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getPacient() {
        return pacient;
    }

    public void setPacient(User pacient) {
        this.pacient = pacient;
    }

    public User getDoctor() {
        return doctor;
    }

    public void setDoctor(User doctor) {
        this.doctor = doctor;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }

    public boolean getAprobat() {
        return aprobat;
    }

    public void setAprobat(boolean aprobat) {
        this.aprobat = aprobat;
    }

    public Timestamp getDataConsultatie() {
        return dataConsultatie;
    }

    public void setDataConsultatie(Timestamp dataConsultatie) {
        this.dataConsultatie = dataConsultatie;
    }
}