DROP TABLE IF EXISTS Simptome_Pacient CASCADE;
DROP TABLE IF EXISTS Diagnostic_Pacient CASCADE;
DROP TABLE IF EXISTS Medicatie_Pacient CASCADE;
DROP TABLE IF EXISTS Consultatie CASCADE;
DROP TABLE IF EXISTS Analize CASCADE;
DROP TABLE IF EXISTS Simptome CASCADE;
DROP TABLE IF EXISTS Diagnostic CASCADE;
DROP TABLE IF EXISTS Medicatie CASCADE;
DROP TABLE IF EXISTS Users CASCADE;

-- CREATE DATABASE MDS;
-- GRANT ALL PRIVILEGES ON DATABASE MDS TO glosper;
GRANT ALL PRIVILEGES ON DATABASE MDS TO postgres;
-- CREATE DATABASE MDS;

CREATE TABLE Users (
    USER_ID SERIAL PRIMARY KEY,
    Acronim VARCHAR(10) NOT NULL,
    Rol VARCHAR(20) NOT NULL CHECK (Rol IN ('PACIENT', 'DOCTOR','ADMIN')),
    Email VARCHAR(100) UNIQUE NOT NULL,
    Parola VARCHAR(255) NOT NULL,
    Activ BOOLEAN NOT NULL DEFAULT FALSE
);


CREATE TABLE Simptome (
    ID_SIMPTOM SERIAL PRIMARY KEY,
    Nume VARCHAR(50) NOT NULL,
    Gravitate INT CHECK (Gravitate BETWEEN 1 AND 5),
    Durata INT CHECK (Durata BETWEEN 1 AND 30)
);

CREATE TABLE Diagnostic (
    ID_DIAGNOSTIC SERIAL PRIMARY KEY,
    Nume VARCHAR(50) NOT NULL,
    Gravitate INT CHECK (Gravitate BETWEEN 1 AND 5)
);

CREATE TABLE Medicatie (
    ID_PRESCRIPTIE SERIAL PRIMARY KEY,
    Nume VARCHAR(50) NOT NULL,
    Durata INT CHECK (Durata BETWEEN 1 AND 30),
    Tip_administrare VARCHAR(20) CHECK (Tip_administrare IN ('oral', 'injectabil'))
);

CREATE TABLE Consultatie (
    ID_CONSULTATIE SERIAL PRIMARY KEY,
    ID_Pacient INT NOT NULL,
    ID_Doctor INT NOT NULL,
    Nota TEXT,
    Aprobat BOOLEAN NOT NULL DEFAULT FALSE,
    Data_consultatie TIMESTAMP NOT NULL,
    FOREIGN KEY (ID_Pacient) REFERENCES Users(USER_ID) ON DELETE CASCADE,
    FOREIGN KEY (ID_Doctor) REFERENCES Users(USER_ID) ON DELETE CASCADE
);

CREATE TABLE Analize (
    ID_ANALIZA SERIAL PRIMARY KEY,
    USER_ID INT NOT NULL,
    Tip_Analiza VARCHAR(50) NOT NULL,
    Data_Analiza TIMESTAMP NOT NULL,
    Rezultat VARCHAR(100),
    FOREIGN KEY (USER_ID) REFERENCES Users(USER_ID) ON DELETE CASCADE
);

CREATE TABLE Simptome_Pacient (
    ID SERIAL PRIMARY KEY,
    ID_Pacient INT NOT NULL,
    ID_Simptom INT NOT NULL,
    Data_Raportare TIMESTAMP NOT NULL,
    FOREIGN KEY (ID_Pacient) REFERENCES Users(USER_ID) ON DELETE CASCADE,
    FOREIGN KEY (ID_Simptom) REFERENCES Simptome(ID_SIMPTOM) ON DELETE CASCADE
);

CREATE TABLE Diagnostic_Pacient (
    ID SERIAL PRIMARY KEY,
    ID_Pacient INT NOT NULL,
    ID_Diagnostic INT NOT NULL,
    Probabilitate FLOAT CHECK (Probabilitate BETWEEN 0 AND 1),
    Data_Diagnostic TIMESTAMP NOT NULL,
    ID_Doctor INT,
    FOREIGN KEY (ID_Pacient) REFERENCES Users(USER_ID) ON DELETE CASCADE,
    FOREIGN KEY (ID_Diagnostic) REFERENCES Diagnostic(ID_DIAGNOSTIC) ON DELETE CASCADE,
    FOREIGN KEY (ID_Doctor) REFERENCES Users(USER_ID) ON DELETE CASCADE
);

CREATE TABLE Medicatie_Pacient (
    ID SERIAL PRIMARY KEY,
    ID_Pacient INT NOT NULL,
    ID_Prescriptie INT NOT NULL,
    Data_Prescriere TIMESTAMP NOT NULL,
    Doza VARCHAR(20),
    Frecventa VARCHAR(20),
    ID_Doctor INT,
    FOREIGN KEY (ID_Pacient) REFERENCES Users(USER_ID) ON DELETE CASCADE,
    FOREIGN KEY (ID_Prescriptie) REFERENCES Medicatie(ID_PRESCRIPTIE) ON DELETE CASCADE,
    FOREIGN KEY (ID_Doctor) REFERENCES Users(USER_ID) ON DELETE CASCADE
);


INSERT INTO Users (USER_ID, Acronim, Rol, Email, Parola, Activ) VALUES
(1, 'AP', 'PACIENT', 'ana.pop@email.com', 'ciscosecpa55', TRUE),
(2, 'IM', 'PACIENT', 'ion.matei@email.com', 'ciscoconpa55', TRUE),
(3, 'MP', 'PACIENT', 'maria.pop@email.com', 'Admin01pa55', TRUE),
(4, 'DR', 'PACIENT', 'dan.rosu@email.com', 'pass101', FALSE),
(5, 'EP', 'PACIENT', 'elena.pop@email.com', 'pass202', TRUE),
(6, 'GV', 'PACIENT', 'george.vas@email.com', 'pass303', TRUE),
(7, 'AV', 'PACIENT', 'andrei.vas@email.com', 'pass404', TRUE),
(8, 'CP', 'PACIENT', 'carla.pop@email.com', 'pass505', TRUE),
(9, 'MV', 'PACIENT', 'mihai.vas@email.com', 'pass606', TRUE),
(10, 'SP', 'PACIENT', 'sofia.pop@email.com', 'pass707', TRUE),
(11, 'DR1', 'DOCTOR', 'dr1@email.com', 'drpass1', TRUE),
(12, 'DR2', 'DOCTOR', 'dr2@email.com', 'drpass2', FALSE),
(13, 'ADM', 'ADMIN', 'admin@email.com', 'adminpass', TRUE);





INSERT INTO Simptome (ID_SIMPTOM, Nume, Gravitate, Durata) VALUES
(1, 'Febra', 5, 3),
(2, 'Tuse', 3, 14),
(3, 'Durere cap', 1, 2),
(4, 'Greata', 3, 1),
(5, 'Oboseala', 5, 20),
(6, 'Durere gat', 3, 5),
(7, 'Frisoane', 5, 2),
(8, 'Durere piept', 5, 30),
(9, 'Ameteala', 1, 1),
(10, 'Insomnie', 3, 25);


INSERT INTO Diagnostic (ID_DIAGNOSTIC, Nume, Gravitate) VALUES
(1, 'Gripa', 5),
(2, 'Bronsita', 3),
(3, 'Migrena', 1),
(4, 'Gastrita', 3),
(5, 'Anemie', 5),
(6, 'Faringita', 3),
(7, 'Pneumonie', 5),
(8, 'Hipertensiune', 5),
(9, 'Vertij', 1),
(10, 'Insomnie', 3);


INSERT INTO Medicatie (ID_PRESCRIPTIE, Nume, Durata, Tip_administrare) VALUES
(1, 'Paracetamol', 3, 'oral'),
(2, 'Ibuprofen', 5, 'oral'),
(3, 'Codeina', 10, 'oral'),
(4, 'Omeprazol', 30, 'oral'),
(5, 'Fier', 20, 'oral'),
(6, 'Amoxicilina', 7, 'oral'),
(7, 'Azitromicina', 5, 'oral'),
(8, 'Amlodipina', 30, 'oral'),
(9, 'Betahistina', 7, 'oral'),
(10, 'Melatonina', 15, 'oral');

INSERT INTO Analize (ID_ANALIZA, USER_ID, Tip_Analiza, Data_Analiza, Rezultat) VALUES
(1, 1, 'Hemograma', '2025-03-01 10:00', 'Normal'),
(2, 2, 'Glicemie', '2025-03-02 09:00', '110 mg/dL'),
(3, 3, 'Radiografie', '2025-03-03 11:00', 'Fara anomalii'),
(4, 4, 'Hemograma', '2025-03-04 08:00', 'Anemie usoara'),
(5, 5, 'Glicemie', '2025-03-05 10:00', '90 mg/dL'),
(6, 6, 'Radiografie', '2025-03-06 12:00', 'Pneumonie'),
(7, 7, 'Hemograma', '2025-03-07 09:00', 'Normal'),
(8, 8, 'Glicemie', '2025-03-08 11:00', '120 mg/dL'),
(9, 9, 'Radiografie', '2025-03-09 10:00', 'Fara anomalii'),
(10, 10, 'Hemograma', '2025-03-10 08:00', 'Normal');


INSERT INTO Consultatie (ID_CONSULTATIE, ID_Pacient, ID_Doctor, Nota, Aprobat, Data_consultatie) VALUES
(1, 1, 11, 'Febra mare, analize OK', TRUE, '2025-03-11 09:00'),
(2, 2, 11, 'Tuse persistenta', FALSE, '2025-03-11 10:00'),
(3, 3, 11, 'Durere cap, analize OK', TRUE, '2025-03-11 11:00'),
(4, 4, 12, 'Greata, anemie detectata', FALSE, '2025-03-12 09:00'),
(5, 5, 12, 'Oboseala, analize OK', TRUE, '2025-03-12 10:00'),
(6, 6, 12, 'Durere gat, pneumonie', FALSE, '2025-03-12 11:00'),
(7, 7, 11, 'Frisoane, analize OK', TRUE, '2025-03-13 09:00'),
(8, 8, 11, 'Durere piept, glicemie', FALSE, '2025-03-13 10:00'),
(9, 9, 12, 'Ameteala, analize OK', TRUE, '2025-03-13 11:00'),
(10, 10, 12, 'Insomnie, analize OK', TRUE, '2025-03-14 09:00');

INSERT INTO Simptome_Pacient (ID, ID_Pacient, ID_Simptom, Data_Raportare) VALUES
(1, 1, 1, '2025-03-01 08:00'),
(2, 2, 2, '2025-03-02 07:00'),
(3, 3, 3, '2025-03-03 09:00'),
(4, 4, 4, '2025-03-04 06:00'),
(5, 5, 5, '2025-03-05 08:00'),
(6, 6, 6, '2025-03-06 07:00'),
(7, 7, 7, '2025-03-07 09:00'),
(8, 8, 8, '2025-03-08 08:00'),
(9, 9, 9, '2025-03-09 07:00'),
(10, 10, 10, '2025-03-10 06:00');

INSERT INTO Diagnostic_Pacient (ID, ID_Pacient, ID_Diagnostic, Data_Diagnostic, ID_Doctor) VALUES
(1, 1, 1, '2025-03-01 09:00', 11),
(2, 2, 2, '2025-03-02 08:00', 11),
(3, 3, 3, '2025-03-03 10:00', 11),
(4, 4, 4, '2025-03-04 07:00', 12),
(5, 5, 5, '2025-03-05 09:00', 12),
(6, 6, 6, '2025-03-06 08:00', 12),
(7, 7, 7, '2025-03-07 10:00', 11),
(8, 8, 8, '2025-03-08 09:00', 11),
(9, 9, 9, '2025-03-09 08:00', 12),
(10, 10, 10, '2025-03-10 07:00', 12);


INSERT INTO Medicatie_Pacient (ID, ID_Pacient, ID_Prescriptie, Data_Prescriere, Doza, Frecventa, ID_Doctor) VALUES
(1, 1, 1, '2025-03-01 09:00', '500 mg', 'La 8 ore', 11),
(2, 2, 2, '2025-03-02 08:00', '400 mg', 'La 12 ore', 11),
(3, 3, 3, '2025-03-03 10:00', '30 mg', 'La 24 ore', 11),
(4, 4, 4, '2025-03-04 07:00', '20 mg', 'La 24 ore', 12),
(5, 5, 5, '2025-03-05 09:00', '50 mg', 'La 24 ore', 12),
(6, 6, 6, '2025-03-06 08:00', '500 mg', 'La 8 ore', 12),
(7, 7, 7, '2025-03-07 10:00', '500 mg', 'La 12 ore', 11),
(8, 8, 8, '2025-03-08 09:00', '5 mg', 'La 24 ore', 11),
(9, 9, 9, '2025-03-09 08:00', '16 mg', 'La 12 ore', 12),
(10, 10, 10, '2025-03-10 07:00', '3 mg', 'La 24 ore', 12);

SELECT s.Nume, s.Gravitate, s.Durata, sp.Data_Raportare
FROM Simptome_Pacient sp
JOIN Simptome s ON sp.ID_Simptom = s.ID_SIMPTOM
WHERE sp.ID_Pacient = 1;
--
-- SELECT d.Nume, d.Gravitate, dp.Data_Diagnostic
-- FROM Diagnostic_Pacient dp
-- JOIN Diagnostic d ON dp.ID_Diagnostic = d.ID_DIAGNOSTIC
-- WHERE dp.ID_Pacient = 1;

