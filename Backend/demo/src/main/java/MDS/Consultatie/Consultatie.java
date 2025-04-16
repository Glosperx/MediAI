//package MDS.Consultatie;
//
//import MDS.Users.User;
//import jakarta.persistence.*;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "Consultatie")
//public class Consultatie {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "ID_CONSULTATIE")
//    private Long idConsultatie;
//
//    @ManyToOne
//    @JoinColumn(name = "ID_Pacient", nullable = false)
//    private User pacient;
//
//    @ManyToOne
//    @JoinColumn(name = "ID_Doctor", nullable = false)
//    private User doctor;
//
//    @Column(name = "Nota")
//    private String nota;
//
//    @Column(name = "Aprobat", nullable = false)
//    private Integer aprobat;
//
//    @Column(name = "Data_consultatie", nullable = false)
//    private LocalDateTime dataConsultatie;
//
//    // Constructors
//    public Consultatie() {}
//
//    public Consultatie(User pacient, User doctor, String nota, Integer aprobat, LocalDateTime dataConsultatie) {
//        this.pacient = pacient;
//        this.doctor = doctor;
//        this.nota = nota;
//        this.aprobat = aprobat;
//        this.dataConsultatie = dataConsultatie;
//    }
//
//    // Getters and Setters
//    public Long getIdConsultatie() {
//        return idConsultatie;
//    }
//
//    public void setIdConsultatie(Long idConsultatie) {
//        this.idConsultatie = idConsultatie;
//    }
//
//    public User getPacient() {
//        return pacient;
//    }
//
//    public void setPacient(User pacient) {
//        this.pacient = pacient;
//    }
//
//    public User getDoctor() {
//        return doctor;
//    }
//
//    public void setDoctor(User doctor) {
//        this.doctor = doctor;
//    }
//
//    public String getNota() {
//        return nota;
//    }
//
//    public void setNota(String nota) {
//        this.nota = nota;
//    }
//
//    public Integer getAprobat() {
//        return aprobat;
//    }
//
//    public void setAprobat(Integer aprobat) {
//        this.aprobat = aprobat;
//    }
//
//    public LocalDateTime getDataConsultatie() {
//        return dataConsultatie;
//    }
//
//    public void setDataConsultatie(LocalDateTime dataConsultatie) {
//        this.dataConsultatie = dataConsultatie;
//    }
//}