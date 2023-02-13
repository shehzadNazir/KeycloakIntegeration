package com.shehzad.keyclaockintegeration.restModels.mappers;

import com.shehzad.keyclaockintegeration.database.models.AppUser;
import com.shehzad.keyclaockintegeration.restModels.directMappings.AppUserInfoDTO;
import com.shehzad.keyclaockintegeration.restModels.user.LoginOutputDTO;
import com.shehzad.keyclaockintegeration.restModels.user.SignupInputDTO;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.HashMap;
import java.util.Map;

public class TransformerFactory {
    private static Map<String, Transformer<?, ?>> map;

    public static <T, U> Transformer<T, U> produceTransformer(Class<T> input, Class<U> output, Object... dependencies) {
        if (map == null) map = new HashMap<>();
        String key = input.getName() + output.getName();
        if (map.get(key) != null) return (Transformer<T, U>) map.get(key);
        if (input == AppUser.class && output == AppUserInfoDTO.class) {
            map.put(input.getName() + output.getName(), new AppUserToUserInfoDTO());
            return (Transformer<T, U>) map.get(key);
        } else if (input == SignupInputDTO.class && output == AppUser.class) {
            map.put(input.getName() + output.getName(), new SignupInputDTOToAppUser().addDependencies(dependencies));
            return (Transformer<T, U>) map.get(key);
        } else if (input == AccessTokenResponse.class && output == LoginOutputDTO.class) {
            map.put(input.getName() + output.getName(), new AccessTokenResponseToLoginOutputDTO());
            return (Transformer<T, U>) map.get(key);
        } else if (input == UserRepresentation.class && output == AppUser.class) {
            map.put(input.getName() + output.getName(), new UserRepresentationToAppUser());
            return (Transformer<T, U>) map.get(key);
        } else if (input == SignupInputDTO.class && output == UserRepresentation.class) {
            map.put(input.getName() + output.getName(), new SignupInputDTOToUserRepresentation().addDependencies(dependencies));
            return (Transformer<T, U>) map.get(key);
        } else {
            throw new IllegalArgumentException("can't find transformer for these classes");
        }
    }
}
