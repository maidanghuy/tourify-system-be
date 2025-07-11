package com.example.tourify_system_be.security;

import java.security.Principal;

public class StompPrincipal implements Principal {
    private final String id;
    private final String username;

    public StompPrincipal(String id, String username) {
        this.id = id;
        this.username = username;
    }

    @Override
    public String getName() {
        return id; // Thường là userId
    }

    public String getUsername() {
        return username;
    }
}
