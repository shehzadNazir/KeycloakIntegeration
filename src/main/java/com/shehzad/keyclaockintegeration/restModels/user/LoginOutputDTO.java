package com.shehzad.keyclaockintegeration.restModels.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jdk.dynalink.linker.LinkerServices;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginOutputDTO {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("expires_in")
    private long accessTokenExpired;
    @JsonProperty("refresh_token")
    private String refreshToken;
    @JsonProperty("refresh_expires_in")
    private long refreshTokenExpires;
    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("session_state")
    private String sessionState;
    @JsonProperty("scope")
    private List<String> scopes;
}