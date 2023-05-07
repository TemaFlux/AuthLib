package com.mojang.authlib.yggdrasil.request;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

public class RefreshRequest {
    private String clientToken, accessToken;
    private GameProfile selectedProfile;
    private boolean requestUser = true;

    public RefreshRequest(YggdrasilUserAuthentication authenticationService) {
        this(authenticationService, null);
    }

    public RefreshRequest(YggdrasilUserAuthentication authenticationService, GameProfile profile) {
        clientToken = authenticationService.getAuthenticationService().getClientToken();
        accessToken = authenticationService.getAuthenticatedToken();
        selectedProfile = profile;
    }
}
