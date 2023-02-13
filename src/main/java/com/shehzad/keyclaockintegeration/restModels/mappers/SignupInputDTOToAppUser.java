package com.shehzad.keyclaockintegeration.restModels.mappers;

import com.shehzad.keyclaockintegeration.database.models.AppUser;
import com.shehzad.keyclaockintegeration.restModels.user.SignupInputDTO;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;

public class SignupInputDTOToAppUser implements Transformer<SignupInputDTO, AppUser> {

    PasswordEncoder passwordEncoder;

    @Override
    public AppUser transform(SignupInputDTO user) {
        AppUser appUser = new AppUser();
        appUser.setPhoneNumber(user.getPhoneNumber());
        appUser.setPassword(passwordEncoder.encode(user.getPassword()));
        appUser.setMiddleName(user.getMiddleName());
        appUser.setFirstName(user.getFirstName());
        appUser.setLastName(user.getLastName());
        appUser.setEmail(user.getEmail());
        appUser.setCreatedAt(new Date());
        appUser.setUpdatedAt(new Date());

        return appUser;
    }

    @Override
    public Transformer<SignupInputDTO, AppUser> addDependencies(Object... dependency) {
        passwordEncoder = (PasswordEncoder) dependency[0];
        return this;
    }
}
