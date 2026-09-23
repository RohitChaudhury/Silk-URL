package Silk_Url.Silk_Url_Service.Model.Dto;

import java.time.Instant;

// DTO for the user enitity to customise the data send to in response
public class UsersDto {
    private String email;
    private String username;
    private String bearerToken;
    private Instant tokenExpireTime;

    public UsersDto() {
    }

    public UsersDto(String email, String username, String jwtToken, Instant time) {
        this.email = email;
        this.username = username;
        this.bearerToken = jwtToken;
        this.tokenExpireTime = time;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    public Instant getTokenExpireTime() {
        return tokenExpireTime;
    }

    public void setTokenExpireTime(Instant tokenExpireTime) {
        this.tokenExpireTime = tokenExpireTime;
    }

}
