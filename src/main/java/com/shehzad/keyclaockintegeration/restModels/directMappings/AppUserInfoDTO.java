package com.shehzad.keyclaockintegeration.restModels.directMappings;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppUserInfoDTO {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("keycloak_user_id")
    private String keycloakUserID;
    @JsonProperty("email")
    private String email;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("middle_name")
    private String middleName;
    @JsonProperty("last_name")
    private String lastName;
    @JsonProperty("phone_number")
    private String phoneNumber;
}

