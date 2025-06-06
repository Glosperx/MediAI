package MDS.Diagnosis;

import java.util.List;
import java.util.Map;

public class Diagnosis_Response {
    private String diagnosis;
    private String medication;
    private Map<String, Double> probabilities;
    private List<String> identifiedSymptoms;  // Simptome prezente
    private List<String> negatedSymptoms;     // Simptome negate

    // Getters si Setteri pentru proprietațile existente
    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getMedication() {
        return medication;
    }

    public void setMedication(String medication) {
        this.medication = medication;
    }

    public Map<String, Double> getProbabilities() {
        return probabilities;
    }

    public void setProbabilities(Map<String, Double> probabilities) {
        this.probabilities = probabilities;
    }

    // Getters si Setteri pentru simptomele identificate
    public List<String> getIdentifiedSymptoms() {
        return identifiedSymptoms;
    }

    public void setIdentifiedSymptoms(List<String> identifiedSymptoms) {
        this.identifiedSymptoms = identifiedSymptoms;
    }

    // Getters si Setteri pentru simptomele negate
    public List<String> getNegatedSymptoms() {
        return negatedSymptoms;
    }

    public void setNegatedSymptoms(List<String> negatedSymptoms) {
        this.negatedSymptoms = negatedSymptoms;
    }

    @Override
    public String toString() {
        return "Diagnostic: " + diagnosis +
                ", Medicament: " + medication +
                ", Probabilități: " + probabilities +
                ", Simptome identificate: " + identifiedSymptoms +
                ", Simptome negate: " + negatedSymptoms;
    }
}