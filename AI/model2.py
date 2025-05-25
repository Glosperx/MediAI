import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
import joblib
import os

class DiagnosisPredictor:
    """
    Clasă pentru antrenarea și evaluarea unui model de predicție a diagnosticului medical bazat pe simptome
    """
    def __init__(self):
        self.model = RandomForestClassifier(n_estimators=100, random_state=15129)
        self.symptoms = []
        self.trained = False
        self.model_path = "diagnosis_model2.pkl"
    
    def train(self, data):
        """
        Antrenează modelul de predicție utilizând Random Forest
        
        Args:
            data: DataFrame cu datele de antrenare
        
        Returns:
            accuracy: Acuratețea modelului pe setul de test
        """
        # Separăm caracteristicile (simptomele) de țintă (diagnosticul)
        X = data.drop(['diagnosis', 'medications'], axis=1)
        y = data['diagnosis']
        
        # Salvăm lista de simptome pentru referințe viitoare
        self.symptoms = X.columns.tolist()
        
        # Împărțim datele în set de antrenare și set de test
        X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=15129)
        
        # Antrenăm modelul
        self.model.fit(X_train, y_train)
        self.trained = True
        
        # Salvăm modelul
        self._save_model()
        
        # Evaluăm și returnăm acuratețea
        y_pred = self.model.predict(X_test)
        accuracy = np.mean(y_pred == y_test)
        
        return accuracy
    
    def predict(self, symptoms_data):
        """
        Prezice diagnosticul pe baza simptomelor
        
        Args:
            symptoms_data: DataFrame sau dicționar cu simptomele pacientului
        
        Returns:
            diagnosis: Diagnosticul prezis
            probabilities: Dicționar cu probabilitățile pentru fiecare diagnostic posibil
        """
        if not self.trained:
            self._load_model()
        
        # Ne asigurăm că simptomele sunt în formatul așteptat
        if isinstance(symptoms_data, dict):
            symptoms_data = pd.DataFrame([symptoms_data])
            
        #Toate simptomele care nu sunt prezente în datele de intrare vor fi setate la 0
        missing_columns = set(self.symptoms) - set(symptoms_data.columns)
        if missing_columns:
            for col in missing_columns:
                symptoms_data[col] = 0
                
        # Reordonăm coloanele pentru a se potrivi cu modelul antrenat
        symptoms_data = symptoms_data[self.symptoms]
        
        # Facem predicția
        diagnosis = self.model.predict(symptoms_data)[0]
        
        # Obținem probabilitățile pentru toate diagnosticele posibile
        prob_values = self.model.predict_proba(symptoms_data)[0]
        classes = self.model.classes_
        probabilities = {diagnosis: prob for diagnosis, prob in zip(classes, prob_values)}
        
        # Sortăm probabilitățile în ordine descrescătoare
        probabilities = dict(sorted(probabilities.items(), key=lambda x: x[1], reverse=True))
        
        return diagnosis, probabilities
    
    def _save_model(self):
        """
        Salvează modelul antrenat pe disc
        """
        model_data = {
            'model': self.model,
            'symptoms': self.symptoms
        }
        joblib.dump(model_data, self.model_path)
    
    def _load_model(self):
        """
        Încarcă modelul antrenat de pe disc
        """
        model_data = joblib.load(self.model_path)
        self.model = model_data['model']
        self.symptoms = model_data['symptoms']
        self.trained = True


def get_diagnosis_and_medication(symptoms_data, data):
    """
    Funcție principală pentru obținerea diagnosticului și medicației pe baza simptomelor
    
    Args:
        symptoms_data: DataFrame sau dicționar cu simptomele pacientului
        data: DataFrame cu datele de antrenare și diagnostice
    
    Returns:
        result: Dicționar cu diagnosticul, probabilitățile și medicația recomandată
    """
    predictor = DiagnosisPredictor()
    
    # Verificăm dacă modelul există, dacă nu, îl antrenăm
    if not os.path.exists(predictor.model_path):
        predictor.train(data)
    else:
        predictor._load_model()
    
    # Facem predicția
    diagnosis, probabilities = predictor.predict(symptoms_data)
    
    # Obținem medicația din setul de date
    medication = data.loc[data['diagnosis'] == diagnosis, 'medications'].values[0]
    
    # Construim rezultatul
    result = {
        'diagnosis': diagnosis,
        'probabilities': probabilities,
        'medication': medication
    }
    
    return result


# Exemplu de utilizare
if __name__ == "__main__":
    from data import load_data, get_user_symptoms
    
    # Setăm seed-ul pentru reproducibilitate
    np.random.seed(15129)
    
    # Încărcăm datele
    data = load_data("data/diagnoses_symptoms_medications.csv")
    
    # Extragem simptomele disponibile
    symptoms = [col for col in data.columns if col not in ['diagnosis', 'medications']]
    
    # Obținem simptomele de la utilizator
    user_symptoms = get_user_symptoms(symptoms)
    
    if user_symptoms is not None:
        # Obținem diagnosticul și medicația
        result = get_diagnosis_and_medication(user_symptoms, data)
        
        # Afișăm rezultatele
        print(f"\nDiagnostic: {result['diagnosis']}")
        print(f"Medicație: {result['medication']}")
        print("\nProbabilități (top 3):")
        
        for i, (diag, prob) in enumerate(result['probabilities'].items()):
            print(f"- {diag}: {prob*100:.2f}%")
            if i >= 2:  # Limităm la primele 3
                break