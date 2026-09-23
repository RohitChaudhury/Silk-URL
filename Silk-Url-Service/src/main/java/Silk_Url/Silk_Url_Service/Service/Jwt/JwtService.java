package Silk_Url.Silk_Url_Service.Service.Jwt;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

import Silk_Url.Silk_Url_Service.Model.Entity.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    private String secretKey;

    public JwtService() {
        try {
            KeyGenerator key = KeyGenerator.getInstance("hmacsha256");
            SecretKey secret = key.generateKey();
            this.secretKey = Base64.getEncoder().encodeToString(secret.getEncoded());
        } catch (NoSuchAlgorithmException error) {
            System.out.println("Unexpected Error while Creating JwtToken: " + error.getMessage());
            secretKey = "{;vz0@*l)^ctuomhh?AIpoA[7GtK7T$yo=0J/7@EJEs";
        }
    }

    // To get the SecretKey object from the encoded string
    public SecretKey generateKey(String secretKey) {
        byte[] keyByte = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyByte);
    }

    // generate a Valid token using the Secret Key for signature
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();

        // genrerating a token from Jwts Builder that's valid for 24 hours.
        return Jwts.builder().claims().add(claims).subject(username).issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.DAYS))).and()
                .signWith(this.generateKey(secretKey)).compact();
    }

    // method to get the username from the token
    public String getUsernameFromToken(String token) {
        return this.extractClaims(token, Claims::getSubject);
    }

    // method to validate the token
    public boolean validateToken(String token, Users user) {
        final String userName = getUsernameFromToken(token);
        return (userName.equals(user.getUsername()) && !isTokenExpired(token));
    }

    private <T> T extractClaims(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(this.generateKey(secretKey))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaims(token, Claims::getExpiration);
    }
}
