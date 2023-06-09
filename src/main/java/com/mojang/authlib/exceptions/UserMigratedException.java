package com.mojang.authlib.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UserMigratedException
extends InvalidCredentialsException {
    public UserMigratedException(String message) {
        super(message);
    }

    public UserMigratedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserMigratedException(Throwable cause) {
        super(cause);
    }
}
