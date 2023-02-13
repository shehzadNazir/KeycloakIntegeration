package com.shehzad.keyclaockintegeration.restModels;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ApiResponse<T> {
    @JsonProperty("response_code")
    int responseCode;
    @JsonProperty("data")
    T data;
    @JsonProperty("message")
    String message;
}
