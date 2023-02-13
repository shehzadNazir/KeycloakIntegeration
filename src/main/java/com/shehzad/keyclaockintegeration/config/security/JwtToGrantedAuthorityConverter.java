package com.shehzad.keyclaockintegeration.config.security;

import com.nimbusds.jose.shaded.gson.internal.LinkedTreeMap;
import com.shehzad.keyclaockintegeration.config.KeycloakProvider;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class JwtToGrantedAuthorityConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    private final KeycloakProvider keycloakProvider;

    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {
        Map<String, Object> convertedClaims = source.getClaims();
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        LinkedTreeMap<String, Object> resourceAccess = (LinkedTreeMap<String, Object>) convertedClaims.get("resource_access");
        List<String> roles = (List<String>) ((LinkedTreeMap<String, Object>) resourceAccess.get(keycloakProvider.getClientID())).get("roles");
        Collection<GrantedAuthority> authorities = converter.convert(source);
        authorities.addAll(roles.stream().map(s -> new SimpleGrantedAuthority("ROLE_" + s)).collect(Collectors.toList()));
        return authorities;
    }
}
