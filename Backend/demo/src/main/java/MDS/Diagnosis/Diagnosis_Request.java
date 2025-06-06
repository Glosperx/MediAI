package MDS.Diagnosis;

public class Diagnosis_Request {
    private Long idPacient;
    private String symptoms;
    private Long idDoctor;

    // Constructor implicit
    public Diagnosis_Request() {}

    // Constructor cu parametri
    public Diagnosis_Request(Long idPacient, String symptoms, Long idDoctor) {
        this.idPacient = idPacient;
        this.symptoms = symptoms;
        this.idDoctor = idDoctor;
    }

    // Getters si Setteri
    public Long getIdPacient() {
        return idPacient;
    }

    public void setIdPacient(Long idPacient) {
        this.idPacient = idPacient;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public Long getIdDoctor() {
        return idDoctor;
    }

    public void setIdDoctor(Long idDoctor) {
        this.idDoctor = idDoctor;
    }
}