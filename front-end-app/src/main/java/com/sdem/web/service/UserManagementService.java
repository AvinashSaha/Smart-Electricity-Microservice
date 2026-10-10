package com.sdem.web.service;

import com.sdem.web.bean.UserView;
import com.sdem.web.exception.UserManagementException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Creating and deleting users, and changing passwords, on top of the in-memory
 * user store. The store itself cannot list users, so the user names and roles
 * are tracked here as well.
 */
@Service
public class UserManagementService {

    private final InMemoryUserDetailsManager manager;
    private final PasswordEncoder encoder;
    private final Map<String, String> roles = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public UserManagementService(InMemoryUserDetailsManager manager, PasswordEncoder encoder,
                                 com.sdem.web.config.FrontEndProperties properties) {
        this.manager = manager;
        this.encoder = encoder;
        if (properties.users() != null) {
            properties.users().forEach(u -> roles.put(u.username(), u.role()));
        }
    }

    public synchronized List<UserView> list() {
        List<UserView> users = new ArrayList<>();
        roles.forEach((name, role) -> users.add(new UserView(name, role)));
        return users;
    }

    public synchronized void create(String username, String rawPassword, String role) {
        if (manager.userExists(username)) {
            throw new UserManagementException("User '" + username + "' already exists");
        }
        manager.createUser(User.withUsername(username).password(encoder.encode(rawPassword)).roles(role).build());
        roles.put(username, role);
    }

    /**
     * Deletes a user. Users cannot delete themselves, and the last administrator cannot be deleted.
     */
    public synchronized void delete(String username, String currentUser) {
        if (!manager.userExists(username)) {
            throw new UserManagementException("User '" + username + "' does not exist");
        }
        if (username.equalsIgnoreCase(currentUser)) {
            throw new UserManagementException("You cannot delete your own account");
        }
        if ("ADMIN".equals(roles.get(username)) && adminCount() == 1) {
            throw new UserManagementException("The last administrator cannot be deleted");
        }
        manager.deleteUser(username);
        roles.remove(username);
    }

    /**
     * Changes a user's password after checking the current one.
     */
    public synchronized void changePassword(String username, String currentPassword, String newPassword) {
        UserDetails user = manager.loadUserByUsername(username);
        if (!encoder.matches(currentPassword, user.getPassword())) {
            throw new UserManagementException("Current password is incorrect");
        }
        if (encoder.matches(newPassword, user.getPassword())) {
            throw new UserManagementException("The new password must be different from the current one");
        }
        manager.updatePassword(user, encoder.encode(newPassword));
    }

    public boolean isAdmin(UserDetails user) {
        return user.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch("ROLE_ADMIN"::equals);
    }

    private long adminCount() {
        return roles.values().stream().filter("ADMIN"::equals).count();
    }
}
