package MDS.Analize;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Analize")
public class Analize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ANALIZA")
    private Long id;

    @Column(name = "USER_ID", nullable = false)
    private Integer userId;

    @Column(name = "Tip_Analiza", nullable = false)
    private String tipAnaliza;

    @Column(name = "Data_Analiza", nullable = false)
    private LocalDateTime dataAnaliza;

    @Column(name = "Rezultat")
    private String rezultat;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTipAnaliza() {
        return tipAnaliza;
    }

    public void setTipAnaliza(String tipAnaliza) {
        this.tipAnaliza = tipAnaliza;
    }

    public LocalDateTime getDataAnaliza() {
        return dataAnaliza;
    }

    public void setDataAnaliza(LocalDateTime dataAnaliza) {
        this.dataAnaliza = dataAnaliza;
    }

    public String getRezultat() {
        return rezultat;
    }

    public void setRezultat(String rezultat) {
        this.rezultat = rezultat;
    }
}