package Silk_Url.Silk_Url_Service.Service.Entity;

import java.util.Arrays;
import java.util.stream.Stream;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import Silk_Url.Silk_Url_Service.Model.Entity.Users;
import Silk_Url.Silk_Url_Service.Service.Entity.BaseService.BaseUserDetailsService;
import Silk_Url.Silk_Url_Service.Service.Jwt.JwtService;

@Service
public class UserService {
    private PasswordEncoder passwordEncoder;
    private BaseUserDetailsService baseService;
    private AuthenticationManager authManager;
    private JwtService jwtService;

    // Regular Expression for Email and Password validation
    private final String EMAIL_VALIDATE = "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@" +
            "[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?" +
            "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$";

    /*
     * Regex for Password that matches the following pattern for Password:
     * ^ # start of line
     * (?=.*[0-9]) # positive lookahead, digit [0-9]
     * (?=.*[a-z]) # positive lookahead, one lowercase character [a-z]
     * (?=.*[A-Z]) # positive lookahead, one uppercase character [A-Z]
     * (?=.*[!@#&()–[{}]:;',?/*~$^+=<>]) # positive lookahead, one of the special
     * character in this [..]
     * . # matches anything
     * {8,20} # length at least 8 characters and maximum of 20 characters
     * $ # end of line
     */
    private final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$";

    // Constructor dependency Injection for Password Encoder,
    // UserDetailsService and Authentication Manager
    public UserService(PasswordEncoder passwordEncoder, BaseUserDetailsService baseService,
            AuthenticationManager authManager, JwtService jwtService) {
        this.passwordEncoder = passwordEncoder;
        this.baseService = baseService;
        this.authManager = authManager;
        this.jwtService = jwtService;
    }

    // Validate Emtpy Credentials of the Users Object
    private String[] isEmptyCredentials(Users users) {
        String[] errors = new String[3];
        if (users.getEmail() == null || users.getEmail().isBlank())
            errors[0] = "emptyEmail";
        if (users.getUsername() == null || users.getUsername().isBlank())
            errors[1] = "emptyUsername";
        if (users.getPassword() == null || users.getPassword().isBlank())
            errors[2] = "emptyPassword";

        return errors;
    }

    // validate Email Pattern using Regex
    private String[] validateEmail(String email) {
        String[] errors = new String[1];
        // Regex Pattern in RFC Style
        if (!email.matches(EMAIL_VALIDATE))
            errors[0] = "invalidEmail";

        return errors;
    }

    private String[] validatePassword(String password) {
        String[] errors = new String[1];
        if (!password.matches(PASSWORD_REGEX))
            errors[0] = "invalidPassword";
        return errors;
    }

    // check if User exists by Email
    private String[] checkUserExistsByEmail(String email) {
        String[] errors = new String[1];
        Users user = baseService.getUserbyEmail(email);

        if (user != null && !user.getUsername().isBlank())
            errors[0] = "userEmailExist";

        return errors;
    }

    // check if User exists by Username
    private String[] checkUserExistsByUsername(String username) {
        String[] errors = new String[1];
        Users user = baseService.loadUserByUsername(username);

        if (user != null && !user.getUsername().isBlank())
            errors[0] = "userUsernameExist";

        return errors;
    }

    // method to validate the credentials
    private String[] validateCredentials(Users users) {
        try {
            String[] emptyErrors = isEmptyCredentials(users);
            String[] emailErrors = validateEmail(users.getEmail());
            String[] userExistByEmailErrors = checkUserExistsByEmail(users.getEmail());
            String[] userExistByUsernameErrors = checkUserExistsByUsername(users.getUsername());
            String[] passErrors = validatePassword(users.getPassword());

            return Stream.of(emptyErrors, emailErrors, userExistByEmailErrors, userExistByUsernameErrors, passErrors)
                    .flatMap(Arrays::stream)
                    .toArray(String[]::new);
        } catch (NullPointerException error) {
            String[] response = {};
            return response;
        }
    }

    // method to create jwtToken for the registered user
    public String getJwtTokenForUser(Users users) {
        return jwtService.generateToken(users.getUsername());
    }

    // method to create a new User
    public String[] createUser(Users users) {
        try {
            // validate credentials of the users
            String[] response = validateCredentials(users);
            boolean check = false;
            for (String res : response) {
                if (res != null) {
                    check = true;
                    break;
                }
            }
            if (!check) {
                users.setPassword(passwordEncoder.encode(users.getPassword())); // Encode the Password
                baseService.createUser(users);
                response[0] = "created";
            }
            return response;
        } catch (Exception error) {
            System.out.println("SERVER ERROR: " + error.getMessage());
            String[] response = { "unexpectedError" };
            return response;
        }
    }

    // method to verify the registered user
    public String[] verifyRegisteredUser(Users user) {
        String[] emptyErrors = isEmptyCredentials(user);
        if (emptyErrors[1] == "emptyUsername" || emptyErrors[2] == "emptyPassword") {
            emptyErrors[0] = null;
            return emptyErrors;
        }

        boolean checkUser = baseService.verifyRegisteredUser(authManager, user);

        if (checkUser) { // User has been successfully authenticated
            emptyErrors[0] = "verified";
        } else {
            emptyErrors[0] = "non-verified";
        }

        return emptyErrors;
    }
}