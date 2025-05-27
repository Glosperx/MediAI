package MDS.Users;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "Users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "Acronim", nullable = false, length = 10)
    private String acronim;

    @Column(name = "Rol", nullable = false, length = 20)
    private String rol;

    @Column(name = "Email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "Parola", nullable = false, length = 255) // Mărit pentru BCrypt hash
    private String parola;

    @Column(name = "activ", nullable = false)
    private boolean activ = false;

    // Constructori
    public User() {}  //JPA

    public User(String acronim, String rol, String email, String parola) {
        this.acronim = acronim;
        this.rol = rol;
        this.email = email;
        this.parola = parola;
        this.activ = false; // Implicit inactiv până la verificare
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getAcronim() { return acronim; }
    public void setAcronim(String acronim) { this.acronim = acronim; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getParola() { return parola; }
    public void setParola(String parola) { this.parola = parola; }

    public boolean isActiv() { return activ; }
    public void setActiv(boolean activ) { this.activ = activ; }

    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(rol));
    }

    @Override
    public String getPassword() {
        return parola;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activ; // Contul este activ doar după verificarea email-ului
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", acronim='" + acronim + '\'' +
                ", rol='" + rol + '\'' +
                ", email='" + email + '\'' +
                ", activ=" + activ +
                '}';
    }
}