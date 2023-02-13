package com.shehzad.keyclaockintegeration.services;

import com.nimbusds.jose.shaded.gson.internal.LinkedTreeMap;
import com.shehzad.keyclaockintegeration.config.KeycloakProvider;
import com.shehzad.keyclaockintegeration.database.models.AccountType;
import com.shehzad.keyclaockintegeration.database.models.AppUser;
import com.shehzad.keyclaockintegeration.restModels.ApiResponse;
import com.shehzad.keyclaockintegeration.restModels.directMappings.AppUserInfoDTO;
import com.shehzad.keyclaockintegeration.restModels.exceptions.InvalidGoogleToken;
import com.shehzad.keyclaockintegeration.restModels.exceptions.InvalidRefreshToken;
import com.shehzad.keyclaockintegeration.restModels.mappers.TransformerFactory;
import com.shehzad.keyclaockintegeration.restModels.user.LoginOutputDTO;
import com.shehzad.keyclaockintegeration.restModels.user.SignupInputDTO;
import com.shehzad.keyclaockintegeration.restModels.user.SignupOutputDTO;
import com.shehzad.keyclaockintegeration.restModels.user.UpdateInputDTO;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class KeycloakAdminClientService {
    private final KeycloakProvider kcProvider;
    private final AppUserService appUserService;
    private final BCryptPasswordEncoder passwordEncoder;
    public final RestTemplate restTemplate;
    public final JwtDecoder jwtDecoder;
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerAddress;

    public KeycloakAdminClientService(KeycloakProvider keycloakProvider, AppUserService appUserService, BCryptPasswordEncoder passwordEncoder, RestTemplate restTemplate, JwtDecoder jwtDecoder) {
        this.kcProvider = keycloakProvider;
        this.appUserService = appUserService;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
        this.jwtDecoder = jwtDecoder;
    }

    public ApiResponse<Long> deleteKeycloakUser(Long userId) {
        Optional<AppUser> appUser = appUserService.findUserById(userId);
        if (appUser.isPresent()) {
            if (!(appUser.get().getKeycloakUserID() == null || appUser.get().getKeycloakUserID().isEmpty())) {
                UsersResource usersResource = kcProvider.getInstance().realm(kcProvider.getRealm()).users();
                try (Response response = usersResource.delete(appUser.get().getKeycloakUserID())) {
                } catch (Exception e) {
                    appUserService.deleteUser(appUser.get());
                    return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), null, String.format("oops something went wrong: %s", e.getMessage()));
                }
            }
            appUserService.deleteUser(appUser.get());
            return new ApiResponse<>(HttpStatus.OK.value(), userId, "User successfully deleted");
        } else {
            return new ApiResponse<>(HttpStatus.NOT_FOUND.value(), null, String.format("cannot find user by id : %d", userId));
        }
    }

    public boolean isCurrentUserAdmin() {
        return !SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().filter(s -> s.equals("ROLE_admin")).collect(Collectors.toList()).isEmpty();
    }

    public String getCurrentUserKcId() {
        return ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getSubject();
    }

    public ApiResponse<AppUserInfoDTO> updateKeycloakUser(Long userId, UpdateInputDTO updateInputDTO) {
        Optional<AppUser> appUser = appUserService.findUserById(userId);
        if (appUser.isEmpty()) {
            return new ApiResponse<>(HttpStatus.NOT_FOUND.value(), null, String.format("cannot find user by id : %d", userId));
        }
        if (!appUser.get().getKeycloakUserID().equals(getCurrentUserKcId())) {
            if (!isCurrentUserAdmin()) {
                return new ApiResponse<>(HttpStatus.FORBIDDEN.value(), null, "You cannot update someone else data you are not admin");
            }
        }

        UsersResource usersResource = kcProvider.getInstance().realm(kcProvider.getRealm()).users();
        if (!appUser.get().getKeycloakUserID().isEmpty()) {
            try {
                UserRepresentation kcUser = new UserRepresentation();
                kcUser.setFirstName(updateInputDTO.getFirstName());
                kcUser.setLastName(updateInputDTO.getLastName());
                kcUser.singleAttribute("phone_number", updateInputDTO.getPhoneNumber()).singleAttribute("middle_name", updateInputDTO.getMiddleName());
                usersResource.get(appUser.get().getKeycloakUserID()).update(kcUser);
            } catch (Exception e) {
                log.error("suppressing error at update keycloak user by id: " + userId, e);
            }
        }
        appUserService.updateUser(appUser.get(), updateInputDTO);
        return new ApiResponse<>(HttpStatus.OK.value(), TransformerFactory.produceTransformer(AppUser.class, AppUserInfoDTO.class, passwordEncoder).transform(appUser.get()), "User successfully updated");
    }

    public ApiResponse<SignupOutputDTO> createKeycloakUser(SignupInputDTO user) {
        if (appUserService.checkUserWithEmail(user.getEmail())) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), null, String.format("User with email: %s already exist", user.getEmail()));
        }
        RealmResource realmResource = kcProvider.getInstance().realm(kcProvider.getRealm());
        UsersResource usersResource = realmResource.users();
        UserRepresentation kcUser = TransformerFactory
                .produceTransformer(SignupInputDTO.class, UserRepresentation.class, createPasswordCredentials(user.getPassword()))
                .transform(user);

        Response response = usersResource.create(kcUser);
        if (response.getStatus() == 201) {
            AppUser appUser = TransformerFactory.produceTransformer(SignupInputDTO.class, AppUser.class, passwordEncoder).transform(user);
            appUser.setKeycloakUserID(getIdFromLocationHeader(response.getHeaderString("Location")));
            appUser.setAccountType(AccountType.LOCAL);
            AppUser savedUser = appUserService.saveUser(appUser);
            response.close();
            return new ApiResponse<>(HttpStatus.OK.value(),
                    SignupOutputDTO.builder().phoneNumber(user.getPhoneNumber())
                            .email(user.getEmail()).firstName(user.getFirstName())
                            .lastName(user.getLastName()).middleName(user.getMiddleName())
                            .id(savedUser.getId()).keycloakUserID(savedUser.getKeycloakUserID())
                            .build(), "User successfull ycreated");
        }
        int code = response.getStatusInfo().getStatusCode();
        LinkedTreeMap<String, Object> message = response.readEntity(LinkedTreeMap.class);

        response.close();

        return new ApiResponse<>(code, null, "" + message.get("errorMessage"));
    }

    private void assignRole(RealmResource realmResource, UserResource userResource) {
        ClientRepresentation clientRepresentation = realmResource.clients().findByClientId(kcProvider.getClientID()).get(0);
        ClientResource clientResource = realmResource.clients().get(clientRepresentation.getId());
        RoleRepresentation roleRepresentation = clientResource.roles().get("user").toRepresentation();
        userResource.roles().clientLevel(clientRepresentation.getId()).add(Arrays.asList(roleRepresentation));
    }

    private String getIdFromLocationHeader(String location) {
        if (location == null || location.isEmpty()) return "";
        String[] locations = location.split("/");
        String[] id = locations[locations.length - 1].split(",");
        if (id.length == 0) return "";

        return id[0];
    }

    private static CredentialRepresentation createPasswordCredentials(String password) {
        CredentialRepresentation passwordCredentials = new CredentialRepresentation();
        passwordCredentials.setTemporary(false);
        passwordCredentials.setType(CredentialRepresentation.PASSWORD);
        passwordCredentials.setValue(password);
        return passwordCredentials;
    }

    public LoginOutputDTO login(String userName, String password) {

        Keycloak keycloak = kcProvider.newKeycloakBuilderWithPasswordCredentials(userName, password);
        LoginOutputDTO loginOutputDTO = TransformerFactory
                .produceTransformer(AccessTokenResponse.class, LoginOutputDTO.class)
                .transform(keycloak.tokenManager().getAccessToken());
        keycloak.close();
        return loginOutputDTO;
    }

    public ApiResponse<Object> logout() {
        Jwt jwt = getCurrentToken();
        kcProvider.getInstance().realm(kcProvider.getRealm()).users()
                .get(jwt.getSubject()).logout();
        return new ApiResponse<>(200, null, "User successfully logged out");
    }

    public Jwt getCurrentToken() {
        return (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }


    private AccessTokenResponse generateTokenByGoogle(String subjectToken) throws InvalidGoogleToken {
        String url = String.format("%s/realms/%s/protocol/openid-connect/token", kcProvider.getServerURL(), kcProvider.getRealm());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("client_id", kcProvider.getClientID());
        body.add("client_secret", kcProvider.getClientSecret());
        body.add("subject_token", subjectToken);
        body.add("subject_issuer", "google");
        body.add("subject_token_type", OAuth2Constants.ACCESS_TOKEN_TYPE);
        body.add("grant_type", OAuth2Constants.TOKEN_EXCHANGE_GRANT_TYPE);
        ResponseEntity<AccessTokenResponse> response = restTemplate.postForEntity(url, new HttpEntity(body, headers), AccessTokenResponse.class);
        AccessTokenResponse accessTokenResponse = response.getBody();
        if (response.getStatusCode().is4xxClientError()) {
            throw new InvalidGoogleToken(accessTokenResponse.getError(), accessTokenResponse.getErrorDescription());
        }

        return accessTokenResponse;
    }

    public LoginOutputDTO loginUserByGoogle(String subjectToken) throws InvalidGoogleToken {
        AccessTokenResponse accessTokenResponse = generateTokenByGoogle(subjectToken);
        String userID = JwtDecoders.fromIssuerLocation(issuerAddress).decode(accessTokenResponse.getToken()).getSubject();
        if (appUserService.userExist(userID)) {
            return TransformerFactory.produceTransformer(AccessTokenResponse.class, LoginOutputDTO.class).transform(accessTokenResponse);
        }
        RealmResource realmResource = kcProvider.getInstance().realm(kcProvider.getRealm());
        UserResource userResource = realmResource.users().get(userID);
        UserRepresentation userRepresentation = userResource.toRepresentation();
        userRepresentation.setEmailVerified(true);
        userResource.update(userRepresentation);
        AppUser appUser = TransformerFactory.produceTransformer(UserRepresentation.class, AppUser.class, passwordEncoder).transform(userRepresentation);
        appUser.setAccountType(AccountType.GOOGLE);
        appUserService.saveGoogleUser(appUser);

        return TransformerFactory.produceTransformer(AccessTokenResponse.class, LoginOutputDTO.class).transform(accessTokenResponse);
    }

    public LoginOutputDTO refreshToken(String refreshToken) throws InvalidRefreshToken {
        String url = String.format("%s/realms/%s/protocol/openid-connect/token", kcProvider.getServerURL(), kcProvider.getRealm());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("client_id", kcProvider.getClientID());
        body.add("client_secret", kcProvider.getClientSecret());
        body.add("refresh_token", refreshToken);
        body.add("grant_type", OAuth2Constants.REFRESH_TOKEN);
        ResponseEntity<AccessTokenResponse> response = restTemplate.postForEntity(url, new HttpEntity(body, headers), AccessTokenResponse.class);
        AccessTokenResponse accessTokenResponse = response.getBody();
        if (response.getStatusCode().is4xxClientError()) {
            throw new InvalidRefreshToken(accessTokenResponse.getError(), accessTokenResponse.getErrorDescription());
        }

        return TransformerFactory.produceTransformer(AccessTokenResponse.class, LoginOutputDTO.class).transform(accessTokenResponse);
    }
}