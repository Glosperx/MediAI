package MDS.Medicatie_Pacient;

import MDS.Users.User;
import MDS.Medicatie.Medicatie;
import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "Medicatie_Pacient")
public class Medicatie_Pacient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ID_Pacient", nullable = false)
    private User pacient;

    @ManyToOne
    @JoinColumn(name = "ID_Prescriptie", nullable = false)
    private Medicatie prescriptie;

    @Column(name = "Data_Prescriere", nullable = false)
    private Timestamp dataPrescriere;

    @Column(name = "Doza", length = 20)
    private String doza;

    @Column(name = "Frecventa", length = 20)
    private String frecventa;

    @ManyToOne
    @JoinColumn(name = "ID_Doctor")
    private User doctor;

    // Constructors
    public Medicatie_Pacient() {}

    public Medicatie_Pacient(User pacient, Medicatie prescriptie, Timestamp dataPrescriere, String doza, String frecventa, User doctor) {
        this.pacient = pacient;
        this.prescriptie = prescriptie;
        this.dataPrescriere = dataPrescriere;
        this.doza = doza;
        this.frecventa = frecventa;
        this.doctor = doctor;
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

    public Medicatie getPrescriptie() {
        return prescriptie;
    }

    public void setPrescriptie(Medicatie prescriptie) {
        this.prescriptie = prescriptie;
    }

    public Timestamp getDataPrescriere() {
        return dataPrescriere;
    }

    public void setDataPrescriere(Timestamp dataPrescriere) {
        this.dataPrescriere = dataPrescriere;
    }

    public String getDoza() {
        return doza;
    }

    public void setDoza(String doza) {
        this.doza = doza;
    }

    public String getFrecventa() {
        return frecventa;
    }

    public void setFrecventa(String frecventa) {
        this.frecventa = frecventa;
    }

    public User getDoctor() {
        return doctor;
    }

    public void setDoctor(User doctor) {
        this.doctor = doctor;
    }
}