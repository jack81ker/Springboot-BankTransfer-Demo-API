package com.example.wide.service;

import com.example.wide.entities.User;
import com.example.wide.repository.UserRepository;
import com.example.wide.security.Principal;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WideUserDetailsService implements UserDetailsService {
    private final Logger logger = LogManager.getLogger(WideUserDetailsService.class);
    private final UserRepository userRepository;

    public WideUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Principal loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Loading user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));

        return Principal.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(user.getRole().getCode())))
                .build();
    }
}
