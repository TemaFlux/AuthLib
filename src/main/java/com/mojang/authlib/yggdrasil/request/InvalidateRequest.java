package com.mojang.authlib.yggdrasil.request;

import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

public class InvalidateRequest {
    private String accessToken, clientToken;

    public InvalidateRequest(YggdrasilUserAuthentication authenticationService) {
        accessToken = authenticationService.getAuthenticatedToken();
        clientToken = authenticationService.getAuthenticationService().getClientToken();
    }
}
