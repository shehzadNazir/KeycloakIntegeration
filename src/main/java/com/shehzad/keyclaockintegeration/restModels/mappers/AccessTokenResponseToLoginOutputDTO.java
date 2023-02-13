package com.shehzad.keyclaockintegeration.restModels.mappers;

import com.shehzad.keyclaockintegeration.restModels.user.LoginOutputDTO;
import org.keycloak.representations.AccessTokenResponse;

import java.util.Arrays;

public class AccessTokenResponseToLoginOutputDTO implements Transformer<AccessTokenResponse, LoginOutputDTO>{
    @Override
    public LoginOutputDTO transform(AccessTokenResponse accessTokenResponse) {
        return new LoginOutputDTO(
                accessTokenResponse.getToken(),
                accessTokenResponse.getExpiresIn(),
                accessTokenResponse.getRefreshToken(),
                accessTokenResponse.getRefreshExpiresIn(),
                accessTokenResponse.getTokenType(),
                accessTokenResponse.getSessionState(),
                Arrays.asList(accessTokenResponse.getScope().split(" "))
        );
    }

    @Override
    public Transformer<AccessTokenResponse, LoginOutputDTO> addDependencies(Object... dependency) {
        return this;
    }
}
