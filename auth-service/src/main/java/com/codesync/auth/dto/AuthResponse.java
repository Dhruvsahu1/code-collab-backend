package com.codesync.auth.dto;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserResponse user;

    public AuthResponse() {}

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }

    public UserResponse getUser() { return user; }
    public void setUser(UserResponse user) { this.user = user; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private AuthResponse response = new AuthResponse();

        public Builder accessToken(String accessToken) { response.accessToken = accessToken; return this; }
        public Builder refreshToken(String refreshToken) { response.refreshToken = refreshToken; return this; }
        public Builder tokenType(String tokenType) { response.tokenType = tokenType; return this; }
        public Builder expiresIn(Long expiresIn) { response.expiresIn = expiresIn; return this; }
        public Builder user(UserResponse user) { response.user = user; return this; }
        public AuthResponse build() { return response; }
    }
}