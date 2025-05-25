package MDS.Simptome;

import jakarta.persistence.*;

@Entity
@Table(name = "Simptome")
public class Simptom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_SIMPTOM")
    private Long id;

    @Column(name = "Nume", nullable = false, length = 50)
    private String nume;

    @Column(name = "Gravitate")
    private Integer gravitate;

    @Column(name = "Durata")
    private Integer durata;

    // Constructors
    public Simptom() {}

    public Simptom(String nume, Integer gravitate, Integer durata) {
        this.nume = nume;
        this.gravitate = gravitate;
        this.durata = durata;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public Integer getGravitate() {
        return gravitate;
    }

    public void setGravitate(Integer gravitate) {
        this.gravitate = gravitate;
    }

    public Integer getDurata() {
        return durata;
    }

    public void setDurata(Integer durata) {
        this.durata = durata;
    }
}