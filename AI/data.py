import pandas as pd
import re
# Păstrăm pentru compatibilitate viitoare
import numpy as np
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split

# Partea 1: Citirea datelor și interfața utilizator

def load_data(file_path):
    """
    Încarcă datele din CSV
    """
    data = pd.read_csv(file_path)
    return data

def remove_diacritics(text):
    """
    Elimină diacriticele din text
    """
    diacritics_map = {
        'ă': 'a', 'â': 'a', 'î': 'i', 'ș': 's', 'ț': 't',
        'Ă': 'A', 'Â': 'A', 'Î': 'I', 'Ș': 'S', 'Ț': 'T'
    }
    for diacritic, replacement in diacritics_map.items():
        text = text.replace(diacritic, replacement)
    return text

def check_symptom_match(text, start_idx, symptom, words):
    """
    Verifică dacă un simptom se potrivește în text, începând de la start_idx.
    Returnează True dacă se potrivește, False altfel.
    """
    symptom_lower = symptom.lower()
    symptom_no_diacritics = remove_diacritics(symptom_lower)
    symptom_words = symptom_no_diacritics.split()
    match_length = len(symptom_words)
    
    # Verificăm dacă mai sunt suficiente cuvinte în text
    if start_idx + match_length <= len(words):
        # Extragem fraza din text
        phrase = ' '.join(words[start_idx:start_idx + match_length])
        phrase_no_diacritics = remove_diacritics(phrase.lower())
        return phrase_no_diacritics == symptom_no_diacritics
    return False

def extract_symptoms_from_text(text, available_symptoms):
    """
    Extrage simptomele din textul introdus de utilizator, gestionând negațiile până la următorul simptom.
    Returnează un dicționar cu simptomele și starea lor (1 pentru prezent, 0 pentru absent/negat).
    """
    text = text.lower().strip()
    # Înlocuim punctuația cu spații
    text = re.sub(r'[.,;:!?()-]', ' ', text)
    
    # Lista de negații
    negations = ['nu', 'fără', 'nici', 'n-am', 'n-avea', 'n-are']
    
    # Dicționar pentru a stoca starea simptomelor (1 = prezent, 0 = absent/negat)
    symptom_status = {symptom: 0 for symptom in available_symptoms}
    
    # Împărțim textul în cuvinte
    words = text.split()
    
    # Variabilă pentru a ține evidența negației active
    is_negated = False
    
    # Procesăm textul cuvânt cu cuvânt
    for i in range(len(words)):
        # Verificăm dacă cuvântul este o negație
        if words[i] in negations:
            is_negated = True
            continue
        
        # Verificăm dacă poziția curentă începe un simptom
        for symptom in available_symptoms:
            if check_symptom_match(text, i, symptom, words):
                # Setăm starea simptomului
                if is_negated:
                    symptom_status[symptom] = 0
                else:
                        symptom_status[symptom] = 1

                # Resetăm negația pentru următorul simptom
                is_negated = False
                break
    
    return symptom_status

def get_user_symptoms(available_symptoms):
    """
    Permite utilizatorului să introducă simptomele și returnează un vector de simptome procesate.
    """
    print("\n=== DESCRIEȚI SIMPTOMELE ===")
    print("Descrieți cum vă simțiți cu propriile cuvinte.")
    print("Exemplu: 'Am febră și tuse, dar nu am durere de cap.' :D")
    
    user_input = input("Descriere: ").strip()
    
    if not user_input:
        print("Nu ați introdus nicio descriere!")
        return None
    
    # Extragem simptomele
    symptom_status = extract_symptoms_from_text(user_input, available_symptoms)
    
    # Afișăm simptomele identificate
    identified_symptoms = [s for s, v in symptom_status.items() if v == 1]
    negated_symptoms = [s for s, v in symptom_status.items() if v == 0 and s.lower() in user_input.lower()]
    
    if identified_symptoms or negated_symptoms:
        print("\nSimptome identificate:")
        for symptom in identified_symptoms:
            print(f"- {symptom} (prezent)")
        for symptom in negated_symptoms:
            print(f"- {symptom} (negat/absent)")
    else:
        print("\nNu am putut identifica simptome specifice. Încercați să folosiți termeni mai clari.")
    
    # Convertim în DataFrame pentru compatibilitate viitoare
    return pd.DataFrame([symptom_status])

# Exemplu de utilizare
if __name__ == "__main__":
    # Încărcăm datele
    data = load_data("data\diagnoses_symptoms_medications.csv")
    
    # Afișăm primele 5 rânduri pentru verificare
    print("\nPrimele 5 rânduri din setul de date:")
    print(data.head())
    
    # Extragem simptomele (coloanele, mai puțin 'diagnosis' și 'medications')
    symptoms = [col for col in data.columns if col not in ['diagnosis', 'medications']]
    print(f"\nAu fost identificate {len(symptoms)} simptome în setul de date.")
    
    # Obținem simptomele de la utilizator
    user_data = get_user_symptoms(symptoms)
    
    if user_data is not None:
        print("\nVectorul de simptome procesat:")
        print(user_data)