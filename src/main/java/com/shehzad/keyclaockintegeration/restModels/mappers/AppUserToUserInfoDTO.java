package com.shehzad.keyclaockintegeration.restModels.mappers;

import com.shehzad.keyclaockintegeration.database.models.AppUser;
import com.shehzad.keyclaockintegeration.restModels.directMappings.AppUserInfoDTO;

public class AppUserToUserInfoDTO implements Transformer<AppUser, AppUserInfoDTO>{
    @Override
    public AppUserInfoDTO transform(AppUser appUser) {
        AppUserInfoDTO appUserInfoDTO = new AppUserInfoDTO();
        appUserInfoDTO.setEmail(appUser.getEmail());
        appUserInfoDTO.setId(appUser.getId());
        appUserInfoDTO.setKeycloakUserID(appUser.getKeycloakUserID());
        appUserInfoDTO.setFirstName(appUser.getFirstName());
        appUserInfoDTO.setLastName(appUser.getLastName());
        appUserInfoDTO.setMiddleName(appUser.getMiddleName());
        appUserInfoDTO.setPhoneNumber(appUser.getPhoneNumber());

        return appUserInfoDTO;
    }

    @Override
    public Transformer<AppUser, AppUserInfoDTO> addDependencies(Object... dependency) {
        return this;
    }
}
