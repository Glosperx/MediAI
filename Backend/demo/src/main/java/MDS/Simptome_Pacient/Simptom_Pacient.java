package MDS.Simptome_Pacient;

import MDS.Users.User;
import MDS.Simptome.Simptom;
import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "Simptome_Pacient")
public class Simptom_Pacient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "ID_Pacient", nullable = false)
    private User pacient;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "ID_Simptom", nullable = false)
    private Simptom simptom;

    @Column(name = "Data_Raportare", nullable = false)
    private Timestamp dataRaportare;

    //Constructor
    public Simptom_Pacient() {}

    public Simptom_Pacient(User pacient, Simptom simptom, Timestamp dataRaportare) {
        this.pacient = pacient;
        this.simptom = simptom;
        this.dataRaportare = dataRaportare;
    }

    //Getters and Setters
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

    public Simptom getSimptom() {
        return simptom;
    }

    public void setSimptom(Simptom simptom) {
        this.simptom = simptom;
    }

    public Timestamp getDataRaportare() {
        return dataRaportare;
    }

    public void setDataRaportare(Timestamp dataRaportare) {
        this.dataRaportare = dataRaportare;
    }
}