package com.mojang.authlib;

public abstract class HttpUserAuthentication
extends BaseUserAuthentication {
    protected HttpUserAuthentication(AuthenticationService authenticationService) {
        super(authenticationService);
    }

    @Override public AuthenticationService getAuthenticationService() {
        return super.getAuthenticationService();
    }
}
