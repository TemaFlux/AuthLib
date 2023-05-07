package com.mojang.authlib.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.security.PublicKey;

@Getter
@RequiredArgsConstructor
public class Property {
    private final String name, value, signature;

    public Property(String value, String name) {
        this(value, name, null);
    }

    public boolean hasSignature() {
        return signature != null;
    }

    public boolean isSignatureValid(PublicKey ignored) {
        return true;
    }
}
