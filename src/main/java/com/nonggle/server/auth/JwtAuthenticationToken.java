package com.nonggle.server.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final Long principal; // userId
    private String credentials; // Token string, not used for authentication after validation

    public JwtAuthenticationToken(Long principal, String credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(true); // Always authenticated if created this way
    }

    public JwtAuthenticationToken(Long principal) {
        super(Collections.emptyList()); // No roles/authorities for now
        this.principal = principal;
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
