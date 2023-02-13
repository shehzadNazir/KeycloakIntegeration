package com.shehzad.keyclaockintegeration.services;

import com.shehzad.keyclaockintegeration.database.models.AppUser;
import com.shehzad.keyclaockintegeration.database.repositories.AppUserRepository;
import com.shehzad.keyclaockintegeration.restModels.directMappings.AppUserInfoDTO;
import com.shehzad.keyclaockintegeration.restModels.mappers.Transformer;
import com.shehzad.keyclaockintegeration.restModels.mappers.TransformerFactory;
import com.shehzad.keyclaockintegeration.restModels.user.UpdateInputDTO;
import com.shehzad.keyclaockintegeration.utils.HelpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AppUserService {
    private final AppUserRepository appUserRepository;

    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public AppUser saveUser(AppUser appUser) {
        return appUserRepository.save(appUser);
    }

    public void saveGoogleUser(AppUser appUser) {
        AppUser appUser1 = appUserRepository.findByKeycloakUserID(appUser.getKeycloakUserID()).orElse(null);
        if (appUser1 == null) {
            appUserRepository.save(appUser);
        } else {
            appUser.setId(appUser1.getId());
            appUserRepository.save(appUser);
        }
    }

    public boolean checkUserWithEmail(String email) {
        return appUserRepository.existsByEmail(email);
    }

    public Optional<AppUser> findUserById(Long userId) {
        return appUserRepository.findById(userId);
    }

    public void deleteUser(AppUser appUser) {
        appUserRepository.deleteById(appUser.getId());
    }

    public void updateUser(AppUser appUser, UpdateInputDTO updateInputDTO) {
        appUser.setFirstName(updateInputDTO.getFirstName());
        appUser.setLastName(updateInputDTO.getLastName());
        appUser.setMiddleName(updateInputDTO.getMiddleName());
        appUser.setPhoneNumber(updateInputDTO.getPhoneNumber());
        appUserRepository.save(appUser);
    }

    public List<AppUserInfoDTO> getAllUserInfo(int page, int size, String sortBy, String sortWith) {
        PageRequest pageRequest = PageRequest.of((page == 0) ? page : (page - 1), (size == 0 || size < 0) ? 20 : size);
        if (!sortWith.trim().isEmpty()) {
            Sort.Direction direction;
            if (sortBy.trim().isEmpty()) {
                direction = Sort.Direction.ASC;
            } else if (sortBy.equalsIgnoreCase("DESC")) {
                direction = Sort.Direction.DESC;
            } else if (sortBy.equalsIgnoreCase("ASC")) {
                direction = Sort.Direction.ASC;
            } else {
                direction = Sort.Direction.ASC;
            }
            pageRequest = pageRequest.withSort(Sort.by(direction, HelpUtil.getColumnNameFromModel(AppUser.class, sortWith, "first_name")));
        }

        Transformer<AppUser, AppUserInfoDTO> transformer = TransformerFactory.produceTransformer(AppUser.class, AppUserInfoDTO.class);

        return appUserRepository.findAll(pageRequest).get().map(transformer::transform).collect(Collectors.toList());
    }

    public boolean userExist(String userID) {
        return appUserRepository.findByKeycloakUserID(userID).isPresent();
    }
}
