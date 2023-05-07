package com.mojang.authlib.legacy;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.HttpUserAuthentication;
import com.mojang.authlib.UserType;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.util.UUIDTypeAdapter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class LegacyUserAuthentication
extends HttpUserAuthentication {
    private static final URL AUTHENTICATION_URL = HttpAuthenticationService.constantURL("https://login.minecraft.net");
    private static final int
    AUTHENTICATION_VERSION = 14,
    RESPONSE_PART_PROFILE_NAME = 2,
    RESPONSE_PART_SESSION_TOKEN = 3,
    RESPONSE_PART_PROFILE_ID = 4;

    private String sessionToken;

    protected LegacyUserAuthentication(LegacyAuthenticationService authenticationService) {
        super(authenticationService);
    }

    @Override
    public void logIn() throws AuthenticationException {
        if (StringUtils.isBlank(getUsername())) throw new InvalidCredentialsException("Invalid username");
        if (StringUtils.isBlank(getPassword())) throw new InvalidCredentialsException("Invalid password");

        Map<String, Object> args = new HashMap<>();
        args.put("user", getUsername());
        args.put("password", getPassword());
        args.put("version", AUTHENTICATION_VERSION);

        String response; try {
            response = getAuthenticationService().performPostRequest(
                AUTHENTICATION_URL,
                HttpAuthenticationService.buildQuery(args),
                "application/x-www-form-urlencoded"
            ).trim();
        } catch (IOException e) {
            throw new AuthenticationException("Authentication server is not responding", e);
        }

        String[] split = response.split(":");
        String profileName, sessionToken, profileId; if (split.length == 5) {
            profileName = split[RESPONSE_PART_PROFILE_NAME];
            sessionToken = split[RESPONSE_PART_SESSION_TOKEN];
            profileId = split[RESPONSE_PART_PROFILE_ID];

            if (StringUtils.isBlank(profileId) || StringUtils.isBlank(profileName) || StringUtils.isBlank(sessionToken))
                throw new AuthenticationException("Unknown response from authentication server: " + response);
        } else throw new InvalidCredentialsException(response);

        setSelectedProfile(new GameProfile(UUIDTypeAdapter.fromString(profileId), profileName));
        this.sessionToken = sessionToken;
        setUserType(UserType.LEGACY);
    }

    @Override public void logOut() {
        super.logOut();
        sessionToken = null;
    }

    @Override public boolean canPlayOnline() {
        return isLoggedIn() && getSelectedProfile() != null && getAuthenticatedToken() != null;
    }

    @Override public GameProfile[] getAvailableProfiles() {
        return getSelectedProfile() == null ? new GameProfile[0] : new GameProfile[] {
            getSelectedProfile()
        };
    }

    @Override public void selectGameProfile(GameProfile profile) {
        throw new UnsupportedOperationException("Game profiles cannot be changed in the legacy authentication service");
    }

    @Override public String getAuthenticatedToken() {
        return sessionToken;
    }

    @Override public String getUserID() {
        return getUsername();
    }

    @Override public LegacyAuthenticationService getAuthenticationService() {
        return (LegacyAuthenticationService) super.getAuthenticationService();
    }
}
