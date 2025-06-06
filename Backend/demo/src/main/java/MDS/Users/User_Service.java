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
    private final User_Repository userRepository; // repository pentru baza de date
    private final PasswordEncoder passwordEncoder; // pentru criptarea parolelor
    private static final List<String> VALID_ROLES = Arrays.asList("ADMIN", "DOCTOR", "PACIENT"); // roluri valide

    // Constructor
    public User_Service(User_Repository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Cauta utilizator dupa email
    public Optional<User> findByEmail(String email) {
        logger.debug("Cautare utilizator cu email: {}", email);
        return userRepository.findByEmail(email); // cauta in baza de date
    }

    // Salveaza utilizator
    @Transactional
    public void save(User user) {
        logger.debug("Salvare utilizator: {}", user);
        userRepository.save(user); // salveaza in baza de date
    }

    // Salveaza utilizator cu parola criptata
    @Transactional
    public void saveWithEncryption(User user) {
        logger.debug("Salvare utilizator cu criptare: {}", user.getEmail());
        user.setParola(passwordEncoder.encode(user.getParola())); // cripteaza parola
        userRepository.save(user); // salveaza utilizator
    }

    // Creeaza utilizator nou
    @Transactional
    public void registerUser(String acronim, String rol, String email, String parola) {
        logger.info("Inregistrare utilizator: email={}, rol={}", email, rol);

        // Verifica rol
        if (!VALID_ROLES.contains(rol.toUpperCase())) {
            logger.error("Rol invalid: {}", rol);
            throw new IllegalArgumentException("Rol invalid: " + rol + ". Roluri valide: " + VALID_ROLES);
        }

        // Verifica email existent
        if (userRepository.existsByEmail(email)) {
            logger.error("Email existent: {}", email);
            throw new IllegalArgumentException("Email inregistrat: " + email);
        }

        // Verifica lungime acronim
        if (acronim != null && acronim.length() > 10) {
            logger.error("Acronim prea lung: {}", acronim);
            throw new IllegalArgumentException("Acronim maxim 10 caractere");
        }

        User user = new User(); // creeaza utilizator
        user.setAcronim(acronim); // seteaza acronim
        user.setRol(rol.toUpperCase()); // seteaza rol
        user.setEmail(email); // seteaza email
        user.setParola(passwordEncoder.encode(parola)); // cripteaza parola
        user.setActiv(false); // cont inactiv pana la activare
        userRepository.save(user); // salveaza utilizator
        logger.info("Utilizator salvat: {}", user);
    }

    // Activeaza cont utilizator
    @Transactional
    public void activateUser(String email) {
        logger.info("Activare utilizator: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("Utilizator negasit: {}", email);
                    return new IllegalArgumentException("Utilizator negasit: " + email);
                });
        user.setActiv(true); // activeaza cont
        userRepository.save(user); // salveaza modificari
        logger.info("Utilizator activat: {}", user);
    }

    // Verifica existenta email
    public boolean existsByEmail(String email) {
        boolean exists = userRepository.existsByEmail(email);
        logger.debug("Verificare email {}: {}", email, exists);
        return exists;
    }

    // Creeaza cont admin
    @Transactional
    public User createAdmin(String email, String parola) {
        logger.info("Creare cont admin: {}", email);

        // Verifica email existent
        if (userRepository.existsByEmail(email)) {
            logger.error("Email existent: {}", email);
            throw new IllegalArgumentException("Email inregistrat: " + email);
        }

        // Valideaza email
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Email invalid");
        }

        // Valideaza parola
        if (parola == null || parola.length() < 8) {
            throw new IllegalArgumentException("Parola minim 8 caractere");
        }

        User admin = new User(); // creeaza admin
        admin.setAcronim("ADM"); // seteaza acronim
        admin.setRol("ADMIN"); // seteaza rol
        admin.setEmail(email); // seteaza email
        admin.setParola(passwordEncoder.encode(parola)); // cripteaza parola
        admin.setActiv(true); // admin activ

        userRepository.save(admin); // salveaza admin
        logger.info("Admin creat: {}", admin);

        return admin; // returneaza admin
    }
}