package com.application.wa9ti.services.auth;

import com.application.wa9ti.models.MyUserDetails;
import com.application.wa9ti.models.Owner;
import com.application.wa9ti.models.User;
import com.application.wa9ti.repositories.OwnerRepository;
import com.application.wa9ti.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email : " + email));

        return new MyUserDetails(user);
    }
}