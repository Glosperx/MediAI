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
    private final User_Repository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final List<String> VALID_ROLES = Arrays.asList("ADMIN", "USER"); // Add "DOCTOR" if allowed

    // Constructor cu dependințe injectate
    public User_Service(User_Repository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByEmail(String email) {
        logger.debug("Căutare utilizator cu email: {}", email);
        return userRepository.findByEmail(email);
    }

    // Metodă pentru salvare fără criptare suplimentară (parola vine deja criptată)
    @Transactional
    public void save(User user) {
        logger.debug("Salvare utilizator: {}", user);
        userRepository.save(user);
    }

    // Metodă pentru salvare cu criptare
    @Transactional
    public void saveWithEncryption(User user) {
        logger.debug("Salvare utilizator cu criptare: {}", user.getEmail());
        user.setParola(passwordEncoder.encode(user.getParola()));
        userRepository.save(user);
    }

    // Înregistrare utilizator nou (cu criptare)
    @Transactional
    public void registerUser(String acronim, String rol, String email, String parola) {
        logger.info("Înregistrare utilizator: email={}, rol={}", email, rol);

        // Validare rol
        if (!VALID_ROLES.contains(rol.toUpperCase())) {
            logger.error("Rol invalid: {}", rol);
            throw new IllegalArgumentException("Rol invalid: " + rol + ". Rolurile valide sunt: " + VALID_ROLES);
        }

        // Verifică email existent
        if (userRepository.existsByEmail(email)) {
            logger.error("Email-ul există deja: {}", email);
            throw new IllegalArgumentException("Email-ul este deja înregistrat: " + email);
        }

        // Validare lungime acronim
        if (acronim != null && acronim.length() > 10) {
            logger.error("Acronim prea lung: {}", acronim);
            throw new IllegalArgumentException("Acronimul nu poate depăși 10 caractere");
        }

        User user = new User();
        user.setAcronim(acronim);
        user.setRol(rol.toUpperCase());
        user.setEmail(email);
        user.setParola(passwordEncoder.encode(parola));
        user.setActiv(false); // Cont inactiv până la activare
        userRepository.save(user);
        logger.info("Utilizator salvat: {}", user);
    }

    // Activare cont utilizator
    @Transactional
    public void activateUser(String email) {
        logger.info("Activare utilizator: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("Utilizator negăsit pentru activare: {}", email);
                    return new IllegalArgumentException("Utilizator negăsit: " + email);
                });
        user.setActiv(true);
        userRepository.save(user);
        logger.info("Utilizator activat: {}", user);
    }

    // Verifică dacă emailul există deja
    public boolean existsByEmail(String email) {
        boolean exists = userRepository.existsByEmail(email);
        logger.debug("Verificare email {}: {}", email, exists);
        return exists;
    }
}