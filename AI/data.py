# data.py - Modulul pentru procesarea datelor si NLP
# Gestioneaza incarcarea datelor, procesarea input-ului utilizator si extragerea simptomelor

import pandas as pd
import re
# Pastram pentru compatibilitate viitoare
import numpy as np
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split

def load_data(file_path):
    """
    Incarca datele din fisierul CSV
    Returneaza: pandas DataFrame cu datele medicale
    """
    data = pd.read_csv(file_path)
    return data

def remove_diacritics(text):
    """
    Elimina diacriticele romanesti din text pentru matching mai bun
    Aceasta functie ajuta la potrivirea input-ului utilizator indiferent de folosirea accentelor
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
    Verifica daca un simptom se potriveste in text incepand de la start_idx
    Permite recunoasterea simptomelor cu mai multe cuvinte (ex: "durere de cap")
    
    Argumente:
        text: textul complet de input
        start_idx: pozitia de start in array-ul de cuvinte
        symptom: simptomul de potrivit
        words: array-ul de cuvinte din text
    
    Returneaza: True daca simptomul se potriveste, False altfel
    """
    symptom_lower = symptom.lower()
    symptom_no_diacritics = remove_diacritics(symptom_lower)
    symptom_words = symptom_no_diacritics.split()
    match_length = len(symptom_words)
    
    # Verificam daca mai sunt suficiente cuvinte in text
    if start_idx + match_length <= len(words):
        # Extragem fraza din text
        phrase = ' '.join(words[start_idx:start_idx + match_length])
        phrase_no_diacritics = remove_diacritics(phrase.lower())
        return phrase_no_diacritics == symptom_no_diacritics
    return False

def extract_symptoms_from_text(text, available_symptoms):
    """
    Extrage simptomele din textul introdus de utilizator, gestionand negatiile
    Aceasta este functia NLP centrala care converteste limbajul natural in caracteristici ML
    
    Argumente:
        text: input-ul utilizatorului care descrie simptomele
        available_symptoms: lista cu toate simptomele posibile
    
    Returneaza: dictionar cu simptomele si starea lor (1=prezent, 0=absent/negat)
    """
    text = text.lower().strip()
    # Inlocuim punctuatia cu spatii pentru separarea mai buna a cuvintelor
    text = re.sub(r'[.,;:!?()-]', ' ', text)
    
    # Cuvintele de negatie romanesti
    negations = ['nu', 'fara', 'nici', 'n-am', 'n-avea', 'n-are']
    
    # Dictionar pentru a stoca starea simptomelor (1 = prezent, 0 = absent/negat)
    symptom_status = {symptom: 0 for symptom in available_symptoms}
    
    # Impartim textul in cuvinte
    words = text.split()
    
    # Variabila pentru a tine evidenta starii de negatie active
    is_negated = False
    
    # Procesam textul cuvant cu cuvant
    for i in range(len(words)):
        # Verificam daca cuvantul curent este o negatie
        if words[i] in negations:
            is_negated = True
            continue
        
        # Verificam daca pozitia curenta incepe un simptom
        for symptom in available_symptoms:
            if check_symptom_match(text, i, symptom, words):
                # Setam starea simptomului pe baza negatiei
                if is_negated:
                    symptom_status[symptom] = 0  # Explicit negat
                else:
                    symptom_status[symptom] = 1  # Prezent

                # Resetam negatia pentru urmatorul simptom
                is_negated = False
                break
    
    return symptom_status

def get_user_symptoms(available_symptoms):
    """
    Functie interactiva pentru a obtine simptomele de la utilizator (pentru linia de comanda)
    """
    print("\n=== DESCRIETI SIMPTOMELE ===")
    print("Descrieti cum va simtiti cu propriile cuvinte.")
    print("Exemplu: 'Am febra si tuse, dar nu am durere de cap.' :D")
    
    user_input = input("Descriere: ").strip()
    
    if not user_input:
        print("Nu ati introdus nicio descriere!")
        return None
    
    # Extragem simptomele
    symptom_status = extract_symptoms_from_text(user_input, available_symptoms)
    
    # Afisam simptomele identificate
    identified_symptoms = [s for s, v in symptom_status.items() if v == 1]
    negated_symptoms = [s for s, v in symptom_status.items() if v == 0 and s.lower() in user_input.lower()]
    
    if identified_symptoms or negated_symptoms:
        print("\nSimptome identificate:")
        for symptom in identified_symptoms:
            print(f"- {symptom} (prezent)")
        for symptom in negated_symptoms:
            print(f"- {symptom} (negat/absent)")
    else:
        print("\nNu am putut identifica simptome specifice. Incercati sa folositi termeni mai clari.")
    
    # Convertim in DataFrame pentru compatibilitatea cu modelul
    return pd.DataFrame([symptom_status])

# Exemplu de utilizare
if __name__ == "__main__":
    # Incarcam datele
    data = load_data("data/diagnoses_symptoms_medications.csv")
    
    # Afisam primele 5 randuri pentru verificare
    print("\nPrimele 5 randuri din setul de date:")
    print(data.head())
    
    # Extragem simptomele (coloanele, mai putin 'diagnosis' si 'medications')
    symptoms = [col for col in data.columns if col not in ['diagnosis', 'medications']]
    print(f"\nAu fost identificate {len(symptoms)} simptome in setul de date.")
    
    # Obtinem simptomele de la utilizator
    user_data = get_user_symptoms(symptoms)
    
    if user_data is not None:
        print("\nVectorul de simptome procesat:")
        print(user_data)