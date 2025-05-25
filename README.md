# Medical Decision Support System

## Overview
This project is an AI-powered platform designed to assist in medical decision-making by suggesting diagnoses based on patient-reported symptoms and medical records. It features a secure login system with three user roles: **patient**, **doctor**, and **admin**. Patients can input symptoms, while doctors validate or adjust AI-generated diagnosis suggestions after reviewing uploaded medical analyses. The system combines artificial intelligence with human expertise to ensure accurate and reliable medical decisions.

## Features
- **Symptom Reporting**: Patients can log symptoms with details like severity and duration
- **AI Diagnosis Suggestions**: AI analyzes symptoms and medical records to propose potential diagnoses using Random Forest algorithm
- **Doctor Validation**: Doctors review and approve/reject AI suggestions, ensuring accuracy with mandatory analysis uploads
- **Role-Based Access**: Secure authentication for patients, doctors, and admins
- **Medical Records Management**: Stores and processes patient analyses and consultation history
- **Email Notifications**: 
  - Account confirmation emails for new user registrations
  - Automated medical reports sent via email containing diagnosis and prescribed medication based on reported symptoms

## Technologies Used

### Backend
- **Java Spring Boot** (with Maven): For building a RESTful API and web application
- **Thymeleaf**: Server-side template engine for rendering dynamic web pages
- **PostgreSQL**: Relational database for storing user data, symptoms, diagnoses, and medical records

### Frontend
- **HTML/CSS**: For user interface development
- **Thymeleaf**: For dynamic content rendering and template processing

### AI/Machine Learning
- **Python**: For developing machine learning models to generate diagnosis suggestions
- **Random Forest Algorithm**: Used for analyzing symptoms and predicting diagnoses
- **CSV Data Processing**: Custom dataset containing symptoms, diagnoses, and medication mappings

### Email Services
- **Email Integration**: For sending account confirmations and medical reports

## System Architecture
The application follows a layered architecture with clear separation between presentation, business logic, and data access layers. The AI component is implemented as a separate Python service that processes symptom data and returns diagnosis suggestions with corresponding medications.

## Getting Started
[Installation and setup instructions would go here]

## Usage
1. **Patient Registration**: New users register and receive email confirmation
2. **Symptom Input**: Patients log their symptoms with detailed information
3. **AI Analysis**: The system processes symptoms using the Random Forest model
4. **Doctor Review**: Medical professionals validate AI suggestions
5. **Report Generation**: Finalized diagnoses and medications are emailed to patients

## Data Security
The system implements role-based access control and secure handling of sensitive medical information in compliance with healthcare data protection standards.