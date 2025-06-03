import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
import joblib
import os

class DiagnosisPredictor:
    """
    Clasa pentru antrenarea si evaluarea unui model de predictie a diagnosticului medical bazat pe simptome
    """
    def __init__(self):
        # Initializam modelul RandomForest cu 100 de arbori si seed fix pentru reproducibilitate
        self.model = RandomForestClassifier(n_estimators=100, random_state=15129)
        # Lista pentru stocarea simptomelor
        self.symptoms = []
        # Indicator pentru a verifica daca modelul a fost antrenat
        self.trained = False
        # Calea unde se va salva modelul antrenat
        self.model_path = "diagnosis_model2.pkl"
    
    def train(self, data):
        """
        Antreneaza modelul folosind datele furnizate
        
        Args:
            data: DataFrame cu datele de antrenare (simptome si diagnostic)
        
        Returns:
            accuracy: Acuratetea modelului pe setul de test
        """
        # Separam simptomele (caracteristici) de diagnostic (tinta)
        X = data.drop(['diagnosis', 'medications'], axis=1)
        y = data['diagnosis']
        
        # Salvam lista de simptome pentru a le folosi la predictie
        self.symptoms = X.columns.tolist()
        
        # Impartim datele in set de antrenare (80%) si test (20%)
        X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=15129)
        
        # Antrenam modelul pe datele de antrenare
        self.model.fit(X_train, y_train)
        self.trained = True
        
        # Salvam modelul antrenat pe disc
        self._save_model()
        
        # Calculam predictiile pe setul de test si returnam acuratetea
        y_pred = self.model.predict(X_test)
        accuracy = np.mean(y_pred == y_test)
        
        return accuracy
    
    def predict(self, symptoms_data):
        """
        Prezice diagnosticul pe baza simptomelor furnizate
        
        Args:
            symptoms_data: DataFrame sau dictionar cu simptomele pacientului
        
        Returns:
            diagnosis: Diagnosticul prezis
            probabilities: Probabilitatile pentru fiecare diagnostic posibil
        """
        # Verificam daca modelul e antrenat, altfel il incarcam
        if not self.trained:
            self._load_model()
        
        # Convertim dictionarul in DataFrame, daca e necesar
        if isinstance(symptoms_data, dict):
            symptoms_data = pd.DataFrame([symptoms_data])
            
        # Adaugam simptome lipsa cu valoarea 0
        missing_columns = set(self.symptoms) - set(symptoms_data.columns)
        if missing_columns:
            for col in missing_columns:
                symptoms_data[col] = 0
                
        # Reordonam coloanele pentru a se potrivi cu ordinea din antrenare
        symptoms_data = symptoms_data[self.symptoms]
        
        # Facem predictia diagnosticului
        diagnosis = self.model.predict(symptoms_data)[0]
        
        # Calculam probabilitatile pentru toate diagnosticele posibile
        prob_values = self.model.predict_proba(symptoms_data)[0]
        classes = self.model.classes_
        probabilities = {diagnosis: prob for diagnosis, prob in zip(classes, prob_values)}
        
        # Sortam probabilitatile in ordine descrescatoare
        probabilities = dict(sorted(probabilities.items(), key=lambda x: x[1], reverse=True))
        
        return diagnosis, probabilities
    
    def _save_model(self):
        """
        Salveaza modelul antrenat si lista de simptome pe disc
        """
        model_data = {
            'model': self.model,
            'symptoms': self.symptoms
        }
        joblib.dump(model_data, self.model_path)
    
    def _load_model(self):
        """
        Incarca modelul si lista de simptome de pe disc
        """
        model_data = joblib.load(self.model_path)
        self.model = model_data['model']
        self.symptoms = model_data['symptoms']
        self.trained = True


def get_diagnosis_and_medication(symptoms_data, data):
    """
    Obtine diagnosticul si medicatia recomandata pe baza simptomelor
    
    Args:
        symptoms_data: DataFrame sau dictionar cu simptomele pacientului
        data: DataFrame cu datele de antrenare (inclusiv medicatia)
    
    Returns:
        result: Dictionar cu diagnosticul, probabilitatile si medicatia
    """
    # Initializam predictorul
    predictor = DiagnosisPredictor()
    
    # Verificam daca modelul exista, altfel il antrenam
    if not os.path.exists(predictor.model_path):
        predictor.train(data)
    else:
        predictor._load_model()
    
    # Obtinem diagnosticul si probabilitatile
    diagnosis, probabilities = predictor.predict(symptoms_data)
    
    # Gasim medicatia corespunzatoare diagnosticului
    medication = data.loc[data['diagnosis'] == diagnosis, 'medications'].values[0]
    
    # Construim rezultatul final
    result = {
        'diagnosis': diagnosis,
        'probabilities': probabilities,
        'medication': medication
    }
    
    return result


# Exemplu de utilizare
if __name__ == "__main__":
    from data import load_data, get_user_symptoms
    
    # Setam seed pentru reproducibilitate
    np.random.seed(15129)
    
    # Incarcam datele din fisierul CSV
    data = load_data("data/diagnoses_symptoms_medications.csv")
    
    # Extragem lista de simptome (excludem diagnosticul si medicatia)
    symptoms = [col for col in data.columns if col not in ['diagnosis', 'medications']]
    
    # Obtinem simptomele introduse de utilizator
    user_symptoms = get_user_symptoms(symptoms)
    
    if user_symptoms is not None:
        # Obtinem diagnosticul si medicatia
        result = get_diagnosis_and_medication(user_symptoms, data)
        
        # Afisam rezultatele
        print(f"\nDiagnostic: {result['diagnosis']}")
        print(f"Medicatie: {result['medication']}")
        print("\nProbabilitati (top 3):")
        
        # Afisam primele 3 probabilitati
        for i, (diag, prob) in enumerate(result['probabilities'].items()):
            print(f"- {diag}: {prob*100:.2f}%")
            if i >= 2:
                break