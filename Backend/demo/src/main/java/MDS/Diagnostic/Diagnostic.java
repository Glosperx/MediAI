package MDS.Diagnostic;

import jakarta.persistence.*;

@Entity
@Table(name = "Diagnostic")
public class Diagnostic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DIAGNOSTIC")
    private Long id;

    @Column(name = "Nume", nullable = false, length = 50)
    private String nume;

    @Column(name = "Gravitate")
    private Integer gravitate;

    //Constructor
    public Diagnostic() {}

    public Diagnostic(String nume, Integer gravitate) {
        this.nume = nume;
        this.gravitate = gravitate;
    }

    //Getters and Setters
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
}