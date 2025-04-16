package MDS.Medicatie;

import jakarta.persistence.*;

@Entity
@Table(name = "Medicatie")
public class Medicatie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PRESCRIPTIE")
    private Long idPrescriptie;

    @Column(name = "Nume", nullable = false, length = 50)
    private String nume;

    @Column(name = "Durata")
    private Integer durata;

    @Column(name = "Tip_administrare")
    private String tipAdministrare;

    // Constructors
    public Medicatie() {}

    public Medicatie(String nume, Integer durata, String tipAdministrare) {
        this.nume = nume;
        this.durata = durata;
        this.tipAdministrare = tipAdministrare;
    }

    // Getters and Setters
    public Long getIdPrescriptie() {
        return idPrescriptie;
    }

    public void setIdPrescriptie(Long idPrescriptie) {
        this.idPrescriptie = idPrescriptie;
    }

    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public Integer getDurata() {
        return durata;
    }

    public void setDurata(Integer durata) {
        this.durata = durata;
    }

    public String getTipAdministrare() {
        return tipAdministrare;
    }

    public void setTipAdministrare(String tipAdministrare) {
        this.tipAdministrare = tipAdministrare;
    }
}