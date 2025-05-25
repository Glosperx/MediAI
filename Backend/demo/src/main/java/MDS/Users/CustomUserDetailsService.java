package MDS.Users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final User_Service userService;

    public CustomUserDetailsService(User_Service userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("Încărcare utilizator cu email: {}", email);
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            logger.error("Utilizatorul cu emailul {} nu a fost găsit", email);
            throw new UsernameNotFoundException("Utilizatorul cu emailul " + email + " nu a fost găsit");
        }
        User user = userOpt.get();
        if (!user.isActiv()) {
            logger.warn("Contul pentru {} nu este activat", email);
            throw new DisabledException("Contul nu este activat");
        }
        logger.debug("Utilizator găsit: {}", user);
        return user;
    }
}