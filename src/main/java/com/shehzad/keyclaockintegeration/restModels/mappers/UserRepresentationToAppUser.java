package com.shehzad.keyclaockintegeration.restModels.mappers;

import com.shehzad.keyclaockintegeration.database.models.AccountType;
import com.shehzad.keyclaockintegeration.database.models.AppUser;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.Date;

public class UserRepresentationToAppUser implements Transformer<UserRepresentation, AppUser> {
    @Override
    public AppUser transform(UserRepresentation userRepresentation) {
        AppUser appUser = new AppUser();
        appUser.setPhoneNumber("");
        appUser.setPassword("");
        appUser.setMiddleName("");
        appUser.setKeycloakUserID(userRepresentation.getId());
        appUser.setFirstName(userRepresentation.getFirstName());
        appUser.setLastName(userRepresentation.getLastName());
        appUser.setEmail(userRepresentation.getEmail());
        appUser.setCreatedAt(new Date());
        appUser.setUpdatedAt(new Date());

        return appUser;
    }

    @Override
    public Transformer<UserRepresentation, AppUser> addDependencies(Object... dependency) {
        return null;
    }
}
