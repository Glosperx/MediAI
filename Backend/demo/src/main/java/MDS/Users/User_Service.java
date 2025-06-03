package MDS.Users;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class User_Service {
    private static final Logger logger = LoggerFactory.getLogger(User_Service.class);
    private final User_Repository userRepository; // repository pentru operatii cu baza de date
    private final PasswordEncoder passwordEncoder; // encoder pentru criptarea parolelor
    private static final List<String> VALID_ROLES = Arrays.asList("ADMIN", "DOCTOR", "PACIENT"); // lista rolurilor valide

    // Constructor
    public User_Service(User_Repository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Cauta un utilizator dupa adresa de email
     * @param email - adresa de email a utilizatorului
     * @return Optional<User> - utilizatorul gasit sau Optional.empty() daca nu exista
     */

    public Optional<User> findByEmail(String email) {
        logger.debug("Căutare utilizator cu email: {}", email);
        return userRepository.findByEmail(email); // cauta utilizatorul in baza de date
    }

    // Salveaza utilizatorul cu parola normala
    @Transactional
    public void save(User user) {
        logger.debug("Salvare utilizator: {}", user);
        userRepository.save(user); // salveaza utilizatorul in baza de date
    }

    // Salveaza utilizatorul si cripteaza parola
    @Transactional
    public void saveWithEncryption(User user) {
        logger.debug("Salvare utilizator cu criptare: {}", user.getEmail());
        user.setParola(passwordEncoder.encode(user.getParola())); // cripteaza parola inainte de salvare
        userRepository.save(user); // salveaza utilizatorul cu parola criptata
    }

    // Creeaza un utilizator nou
    @Transactional
    public void registerUser(String acronim, String rol, String email, String parola) {
        logger.info("Înregistrare utilizator: email={}, rol={}", email, rol);

        // Validare rol
        if (!VALID_ROLES.contains(rol.toUpperCase())) { // verifica daca rolul este valid
            logger.error("Rol invalid: {}", rol);
            throw new IllegalArgumentException("Rol invalid: " + rol + ". Rolurile valide sunt: " + VALID_ROLES);
        }

        // Verifică email existent
        if (userRepository.existsByEmail(email)) { // verifica daca emailul exista deja
            logger.error("Email-ul există deja: {}", email);
            throw new IllegalArgumentException("Email-ul este deja înregistrat: " + email);
        }

        // Validare lungime acronim
        if (acronim != null && acronim.length() > 10) { // verifica lungimea acronimului
            logger.error("Acronim prea lung: {}", acronim);
            throw new IllegalArgumentException("Acronimul nu poate depăși 10 caractere");
        }

        User user = new User(); // creeaza un nou utilizator
        user.setAcronim(acronim); // seteaza acronimul
        user.setRol(rol.toUpperCase()); // seteaza rolul in uppercase
        user.setEmail(email); // seteaza emailul
        user.setParola(passwordEncoder.encode(parola)); // cripteaza si seteaza parola
        user.setActiv(false); // Cont inactiv până la activare
        userRepository.save(user); // salveaza utilizatorul in baza de date
        logger.info("Utilizator salvat: {}", user);
    }

    // Activeaza contul unui utilizator
    @Transactional
    public void activateUser(String email) {
        logger.info("Activare utilizator: {}", email);
        User user = userRepository.findByEmail(email) // cauta utilizatorul dupa email
                .orElseThrow(() -> { // daca nu gaseste utilizatorul, arunca exceptie
                    logger.error("Utilizator negăsit pentru activare: {}", email);
                    return new IllegalArgumentException("Utilizator negăsit: " + email);
                });
        user.setActiv(true); // activeaza contul utilizatorului
        userRepository.save(user); // salveaza modificarea in baza de date
        logger.info("Utilizator activat: {}", user);
    }

    // Verifică dacă emailul există deja
    public boolean existsByEmail(String email) {
        boolean exists = userRepository.existsByEmail(email); // verifica existenta emailului
        logger.debug("Verificare email {}: {}", email, exists);
        return exists; // returneaza true daca emailul exista
    }

    @Transactional
    public User createAdmin(String email, String parola) {
        logger.info("Creare cont admin: {}", email);

        // Verifică doar dacă email-ul există
        if (userRepository.existsByEmail(email)) { // verifica daca emailul exista deja
            logger.error("Email-ul există deja: {}", email);
            throw new IllegalArgumentException("Email-ul este deja înregistrat: " + email);
        }

        // Validare email
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) { // valideaza formatul emailului
            throw new IllegalArgumentException("Email invalid");
        }

        // Validare parolă
        if (parola == null || parola.length() < 8) { // verifica lungimea parolei
            throw new IllegalArgumentException("Parola trebuie să aibă cel puțin 8 caractere");
        }

        User admin = new User(); // creeaza un nou utilizator admin
        admin.setAcronim("ADM"); // seteaza acronimul pentru admin
        admin.setRol("ADMIN"); // seteaza rolul de administrator
        admin.setEmail(email); // seteaza emailul
        admin.setParola(passwordEncoder.encode(parola)); // cripteaza si seteaza parola
        admin.setActiv(true); // adminul este activ din start

        userRepository.save(admin); // salveaza adminul in baza de date
        logger.info("Admin creat cu succes: {}", admin);

        return admin; // returneaza utilizatorul admin creat
    }
}