# app.py - Server Flask pentru API-ul medical
# Acesta este serverul web principal care primeste cereri si returneaza predictii

from flask import Flask, request, jsonify
from flask_cors import CORS
import pandas as pd
import model2
import data

app = Flask(__name__)
CORS(app)  # Permite cereri cross-origin de la Spring

# Incarcam dataset-ul medical care contine diagnostice, simptome si medicatii
dataset = data.load_data("data/diagnoses_symptoms_medications.csv")

# Extragem lista de simptome disponibile din coloanele dataset-ului
# (toate coloanele mai putin 'diagnosis' si 'medications' sunt simptome)
available_symptoms = [col for col in dataset.columns if col not in ['diagnosis', 'medications']]

@app.route('/predict', methods=['POST'])
def predict():
    """
    Endpoint principal pentru predictii - primeste text cu simptome si returneaza diagnostic
    """
    try:
        # Primim datele JSON din cerere
        input_data = request.get_json()
        symptoms_text = input_data.get('symptoms', '')
        if not symptoms_text:
            return jsonify({'error': 'Symptomele lipsesc'}), 400

        # Procesam textul pentru a extrage simptomele folosind NLP
        symptoms_data = data.extract_symptoms_from_text(symptoms_text, available_symptoms)

        # Identificam simptomele prezente si negate pentru feedback utilizator
        identified_symptoms = [s for s, v in symptoms_data.items() if v == 1]
        negated_symptoms = [s for s, v in symptoms_data.items() if v == 0 and s.lower() in symptoms_text.lower()]

        # Convertim in DataFrame pentru modelul ML
        symptoms_df = pd.DataFrame([symptoms_data])

        # Obtinem diagnosticul si medicatia folosind modelul antrenat
        result = model2.get_diagnosis_and_medication(symptoms_df, dataset)

        # Sortam probabilitatile in ordine descrescatoare si luam primele 3
        probabilities = result['probabilities']
        sorted_probs = sorted(probabilities.items(), key=lambda x: x[1], reverse=True)
        top_3_probs = {diag: prob for diag, prob in sorted_probs[:3]}  # Dictionar care pastreaza ordinea

        # Formam raspunsul JSON
        response = {
            'diagnosis': result['diagnosis'],
            'medication': result['medication'],
            'probabilities': top_3_probs,
            'identified_symptoms': identified_symptoms,
            'negated_symptoms': negated_symptoms
        }

        return jsonify(response), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/health', methods=['GET'])
def health_check():
    """Endpoint pentru verificarea starii serviciului"""
    return jsonify({'status': 'OK', 'message': 'Flask AI Service is running'}), 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)