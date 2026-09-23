package Silk_Url.Silk_Url_Service.Service.Entity.BaseService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import Silk_Url.Silk_Url_Service.Model.Entity.Users;
import Silk_Url.Silk_Url_Service.Repository.UserRepository;

@Service
public class BaseUserDetailsService implements UserDetailsService {
    private UserRepository repository;

    public BaseUserDetailsService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Users loadUserByUsername(String username) {
        return repository.findUsersByUsername(username);
    }

    // method to fetch user by email
    public Users getUserbyEmail(String email) {
        return repository.findUsersByEmail(email);
    }

    // create a new User in the DB
    public Users createUser(Users user) {
        return repository.save(user);
    }

    // method to verify registered user
    public boolean verifyRegisteredUser(AuthenticationManager auth, Users users) {
        Authentication authentication = auth
                .authenticate(new UsernamePasswordAuthenticationToken(users.getUsername(), users.getPassword()));
        return authentication == null ? false : true;
    }
}
