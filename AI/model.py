import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import roc_curve, auc
from sklearn.preprocessing import label_binarize
import matplotlib.pyplot as plt
import joblib
import os

class DiagnosisPredictor:
    """
    Clasa pentru antrenarea si evaluarea unui model de predictie a diagnosticului medical bazat pe simptome
    """
    def __init__(self):
        self.model = RandomForestClassifier(n_estimators=100, random_state=15129)
        self.symptoms = []
        self.trained = False
        self.model_path = "diagnosis_model2.pkl"
    
    def train(self, data):
        """
        Antreneaza modelul de predictie utilizand Random Forest
        
        Args:
            data: DataFrame cu datele de antrenare
        
        Returns:
            accuracy: Acuratetea modelului pe setul de test
            X_test: Setul de test cu caracteristici
            y_test: Etichetele reale pentru setul de test
        """
        # Separam caracteristicile (simptomele) de tinta (diagnosticul)
        X = data.drop(['diagnosis', 'medications'], axis=1, errors='ignore')
        y = data['diagnosis']
        
        # Verificam ca toate coloanele din X sunt numerice
        if not all(X.dtypes.apply(lambda x: np.issubdtype(x, np.number))):
            raise ValueError("Toate coloanele din X trebuie sa fie numerice. Verifica setul de date.")
        
        # Salvam lista de simptome pentru referinte viitoare
        self.symptoms = X.columns.tolist()
        
        # Impartim datele in set de antrenare si set de test cu stratificare
        X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.3, random_state=15129, stratify=y)
        
        # Antrenam modelul
        self.model.fit(X_train, y_train)
        self.trained = True
        
        # Salvam modelul
        self._save_model()
        
        # Evaluam si returnam acuratetea
        y_pred = self.model.predict(X_test)
        accuracy = np.mean(y_pred == y_test)
        
        return accuracy, X_test, y_test
    
    def predict(self, symptoms_data):
        """
        Prezice diagnosticul pe baza simptomelor
        
        Args:
            symptoms_data: DataFrame sau dictionar cu simptomele pacientului
        
        Returns:
            diagnosis: Diagnosticul prezis
            probabilities: Dictionar cu probabilitatile pentru fiecare diagnostic posibil
        """
        if not self.trained:
            self._load_model()
        
        # Ne asiguram ca simptomele sunt in formatul asteptat
        if isinstance(symptoms_data, dict):
            symptoms_data = pd.DataFrame([symptoms_data])
            
        # Toate simptomele care nu sunt prezente in datele de intrare vor fi setate la 0
        missing_columns = set(self.symptoms) - set(symptoms_data.columns)
        if missing_columns:
            for col in missing_columns:
                symptoms_data[col] = 0
                
        # Reordonam coloanele pentru a se potrivi cu modelul antrenat
        symptoms_data = symptoms_data[self.symptoms]
        
        # Verificam ca toate coloanele din symptoms_data sunt numerice
        if not all(symptoms_data.dtypes.apply(lambda x: np.issubdtype(x, np.number))):
            raise ValueError("Toate coloanele din symptoms_data trebuie sa fie numerice.")
        
        # Facem predictia
        diagnosis = self.model.predict(symptoms_data)[0]
        
        # Obtinem probabilitatile pentru toate diagnosticele posibile
        prob_values = self.model.predict_proba(symptoms_data)[0]
        classes = self.model.classes_
        probabilities = {diagnosis: prob for diagnosis, prob in zip(classes, prob_values)}
        
        # Sortam probabilitatile in ordine descrescatoare
        probabilities = dict(sorted(probabilities.items(), key=lambda x: x[1], reverse=True))
        
        return diagnosis, probabilities
    
    def plot_roc_curve(self, X_test, y_test):
        """
        Genereaza si afiseaza curba ROC si scorul AUC pentru modelul antrenat
        
        Args:
            X_test: Setul de test cu caracteristici
            y_test: Etichetele reale pentru setul de test
        """
        # Verificam ca X_test contine doar valori numerice
        if not all(X_test.dtypes.apply(lambda x: np.issubdtype(x, np.number))):
            raise ValueError("Toate coloanele din X_test trebuie sa fie numerice.")
        
        # Binarizam etichetele pentru clasificare multi-clasa
        y_test_bin = label_binarize(y_test, classes=self.model.classes_)
        n_classes = y_test_bin.shape[1]
        
        # Obtinem probabilitatile pentru setul de test
        y_score = self.model.predict_proba(X_test)
        
        # Calculam curba ROC si AUC pentru fiecare clasa
        fpr = dict()
        tpr = dict()
        roc_auc = dict()
        valid_classes = []
        for i in range(n_classes):
            if y_test_bin[:, i].sum() > 0:  # Verificam daca exista mostre pozitive
                fpr[i], tpr[i], _ = roc_curve(y_test_bin[:, i], y_score[:, i])
                roc_auc[i] = auc(fpr[i], tpr[i])
                valid_classes.append(i)
            else:
                print(f"Clasa {self.model.classes_[i]} nu are mostre pozitive in setul de test; se omite ROC.")
        
        # Plotam curba ROC pentru clasele valide
        plt.figure(figsize=(10, 8))
        colors = ['blue', 'red', 'green', 'purple', 'orange']  # Culori pentru clase
        for i in valid_classes:
            plt.plot(fpr[i], tpr[i], color=colors[i % len(colors)], lw=2,
                     label=f'Curba ROC pentru {self.model.classes_[i]} (AUC = {roc_auc[i]:.2f})')
        
        # Adaugam linia de referinta (clasificator aleator)
        plt.plot([0, 1], [0, 1], 'k--', lw=2)
        
        # Configuram graficul
        plt.xlim([0.0, 1.0])
        plt.ylim([0.0, 1.05])
        plt.xlabel('Rata de Fals Pozitive (FPR)')
        plt.ylabel('Rata de Adevarate Pozitive (TPR)')
        plt.title('Curba ROC pentru Clasificare Multi-Clasa')
        plt.legend(loc="lower right")
        plt.grid(True)
        plt.show()
        
        # Returnam scorurile AUC pentru clasele valide
        return {self.model.classes_[i]: roc_auc[i] for i in valid_classes}
    
    def _save_model(self):
        """
        Salveaza modelul antrenat pe disc
        """
        model_data = {
            'model': self.model,
            'symptoms': self.symptoms
        }
        joblib.dump(model_data, self.model_path)
    
    def _load_model(self):
        """
        Incarca modelul antrenat de pe disc
        """
        model_data = joblib.load(self.model_path)
        self.model = model_data['model']
        self.symptoms = model_data['symptoms']
        self.trained = True


def get_diagnosis_and_medication(symptoms_data, data):
    """
    Functie principala pentru obtinerea diagnosticului si medicamentului pe baza simptomelor
    
    Args:
        symptoms_data: DataFrame sau dictionar cu simptomele pacientului
        data: DataFrame cu datele de antrenare si diagnostice
    
    Returns:
        result: Dictionar cu diagnosticul, probabilitatile si medicatia recomandata
    """
    predictor = DiagnosisPredictor()
    
    # Verificam ca toate coloanele din data (exceptand diagnosis si medications) sunt numerice
    X = data.drop(['diagnosis', 'medications'], axis=1, errors='ignore')
    if not all(X.dtypes.apply(lambda x: np.issubdtype(x, np.number))):
        raise ValueError("Toate coloanele din setul de date (exceptand 'diagnosis' si 'medications') trebuie sa fie numerice.")
    
    # Verificam daca modelul exista, daca nu, il antrenam
    if not os.path.exists(predictor.model_path):
        accuracy, X_test, y_test = predictor.train(data)
        # Afisam curba ROC si scorurile AUC
        roc_auc = predictor.plot_roc_curve(X_test, y_test)
        print(f"Acuratete: {accuracy:.2f}")
        print(f"Scoruri AUC: {roc_auc}")
    else:
        # Incarcam modelul
        predictor._load_model()
        # Generam X_test si y_test pentru a afisa curba ROC
        X = data.drop(['diagnosis', 'medications'], axis=1, errors='ignore')
        y = data['diagnosis']
        _, X_test, _, y_test = train_test_split(X, y, test_size=0.3, random_state=15129, stratify=y)
        # Afisam curba ROC si scorurile AUC
        roc_auc = predictor.plot_roc_curve(X_test, y_test)
        # Calculam si afisam acuratetea
        y_pred = predictor.model.predict(X_test)
        accuracy = np.mean(y_pred == y_test)
        print(f"Acuratete: {accuracy:.2f}")
        print(f"Scoruri AUC: {roc_auc}")
    
    # Facem predictia
    diagnosis, probabilities = predictor.predict(symptoms_data)
    
    # Obtinem medicatia din setul de date
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
    
    # Setam seed-ul pentru reproducibilitate
    np.random.seed(15129)
    
    # Incarcam datele
    data = load_data("data\diagnoses_symptoms_medications_extended.csv")
    
    # Extragem simptomele disponibile
    symptoms = [col for col in data.columns if col not in ['diagnosis', 'medications']]
    
    # Obtinem simptomele de la utilizator
    user_symptoms = get_user_symptoms(symptoms)
    
    if user_symptoms is not None:
        # Obtinem diagnosticul si medicatia
        result = get_diagnosis_and_medication(user_symptoms, data)
        
        # Afisam rezultatele
        print(f"\nDiagnostic: {result['diagnosis']}")
        print(f"Medicatie: {result['medication']}")
        print("\nProbabilitati (top 3):")
        
        for i, (diag, prob) in enumerate(result['probabilities'].items()):
            print(f"- {diag}: {prob*100:.2f}%")
            if i >= 2:  # Limitam la primele 3
                break