package com.example.auth;

import com.example.role.Role;
import com.example.role.RoleRepository;
import com.example.role.RoleService;
import com.example.user.User;
import com.example.user.UserRepository;
import com.example.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public void activateUser(User user) {
        // hash password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Optional<Role> role = roleService.findByName("STUDENT");
        if (role.isEmpty()) {
            role = Optional.of(roleRepository.save(Role.builder().name("STUDENT").build()));
        }
        // default role
        user.setRoles(List.of(role.get()));
        userRepository.save(user);
    }
}
