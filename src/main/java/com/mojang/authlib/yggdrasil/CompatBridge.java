package com.mojang.authlib.yggdrasil;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pro.gravit.launcher.profiles.PlayerProfile;
import pro.gravit.launcher.request.auth.CheckServerRequest;
import pro.gravit.launcher.request.auth.JoinServerRequest;
import pro.gravit.launcher.request.uuid.BatchProfileByUsernameRequest;
import pro.gravit.launcher.request.uuid.ProfileByUUIDRequest;
import pro.gravit.launcher.request.uuid.ProfileByUsernameRequest;

import java.util.UUID;

// @Deprecated
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CompatBridge {
    public static final int PROFILES_MAX_BATCH_SIZE = 128;

    public static CompatProfile checkServer(String username, String serverID){
        return CompatProfile.fromPlayerProfile(new CheckServerRequest(username, serverID).request().playerProfile);
    }

    public static boolean joinServer(String username, String accessToken, String serverID) {
        return new JoinServerRequest(username, accessToken, serverID).request().allow;
    }

    public static CompatProfile profileByUsername(String username) {
        return CompatProfile.fromPlayerProfile(new ProfileByUsernameRequest(username).request().playerProfile);
    }

    public static CompatProfile profileByUUID(UUID uuid) {
        return CompatProfile.fromPlayerProfile(new ProfileByUUIDRequest(uuid).request().playerProfile);
    }

    public static CompatProfile[] profilesByUsername(String... usernames) {
        PlayerProfile[] profiles = new BatchProfileByUsernameRequest(usernames).request().playerProfiles;
        CompatProfile[] resultProfiles = new CompatProfile[profiles.length];

        for (int i = 0; i < profiles.length; i++) resultProfiles[i] = CompatProfile.fromPlayerProfile(profiles[i]);
        return resultProfiles;
    }
}
