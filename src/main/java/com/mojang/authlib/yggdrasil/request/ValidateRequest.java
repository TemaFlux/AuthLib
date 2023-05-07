package com.mojang.authlib.yggdrasil.request;

import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

public class ValidateRequest {
    private String clientToken, accessToken;

    public ValidateRequest(YggdrasilUserAuthentication authenticationService) {
        clientToken = authenticationService.getAuthenticationService().getClientToken();
        accessToken = authenticationService.getAuthenticatedToken();
    }
}
