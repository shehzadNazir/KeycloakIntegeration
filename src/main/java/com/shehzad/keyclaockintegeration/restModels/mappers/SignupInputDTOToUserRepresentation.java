package com.shehzad.keyclaockintegeration.restModels.mappers;

import com.shehzad.keyclaockintegeration.restModels.user.SignupInputDTO;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.Collections;

public class SignupInputDTOToUserRepresentation implements Transformer<SignupInputDTO, UserRepresentation> {

    private CredentialRepresentation credentialRepresentation;

    @Override
    public UserRepresentation transform(SignupInputDTO signupInputDTO) {
        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setCredentials(Collections.singletonList(credentialRepresentation));
        kcUser.setFirstName(signupInputDTO.getFirstName());
        kcUser.setLastName(signupInputDTO.getLastName());
        kcUser.singleAttribute("phone_number", signupInputDTO.getPhoneNumber()).singleAttribute("middle_name", signupInputDTO.getMiddleName());
        kcUser.setEmail(signupInputDTO.getEmail());
        kcUser.setEnabled(true);
        kcUser.setEmailVerified(true);
        kcUser.setRequiredActions(Collections.emptyList());
        kcUser.setGroups(Collections.emptyList());


        return kcUser;
    }

    @Override
    public Transformer<SignupInputDTO, UserRepresentation> addDependencies(Object... dependency) {
        credentialRepresentation = (CredentialRepresentation) dependency[0];
        return this;
    }
}
