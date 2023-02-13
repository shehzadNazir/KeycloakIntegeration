package com.shehzad.keyclaockintegeration.restModels.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class LoginInputDTO {
    @JsonProperty("email")
    private String email;
    @JsonProperty("password")
    private String password;
}
