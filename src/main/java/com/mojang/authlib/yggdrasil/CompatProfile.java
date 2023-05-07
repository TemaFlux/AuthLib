package com.mojang.authlib.yggdrasil;

import java.util.UUID;
import pro.gravit.launcher.Launcher;
import pro.gravit.launcher.profiles.PlayerProfile;
import pro.gravit.launcher.profiles.Texture;
import pro.gravit.utils.helper.SecurityHelper;

// @Deprecated
public class CompatProfile {
    public static final String
    SKIN_URL_PROPERTY = "skinURL",
    SKIN_DIGEST_PROPERTY = "skinDigest",
    CLOAK_URL_PROPERTY = "cloakURL",
    CLOAK_DIGEST_PROPERTY = "cloakDigest";

    public final UUID uuid;
    public final String uuidHash, username, skinURL, skinDigest, cloakURL, cloakDigest;

    public CompatProfile(UUID uuid, String username, String skinURL, String skinDigest, String cloakURL, String cloakDigest) {
        this.uuid = uuid;
        uuidHash = Launcher.toHash(uuid);
        this.username = username;
        this.skinURL = skinURL;
        this.skinDigest = skinDigest;
        this.cloakURL = cloakURL;
        this.cloakDigest = cloakDigest;
    }

    public static CompatProfile fromPlayerProfile(PlayerProfile profile) {
        if (profile == null) return null;

        Texture skin = profile.assets.getOrDefault("SKIN", profile.assets.get("skin"));
        Texture cloak = profile.assets.getOrDefault("CLOAK", profile.assets.get("cloak"));

        return new CompatProfile(
            profile.uuid,
            profile.username,
            skin == null ? null : skin.url, skin == null ? null : SecurityHelper.toHex(skin.digest),
            cloak == null ? null : cloak.url, cloak == null ? null : SecurityHelper.toHex(cloak.digest)
        );
    }

    public int countProperties() {
        int count = 0;
        if (skinURL != null) count++;
        if (skinDigest != null) count++;
        if (cloakURL != null) count++;
        if (cloakDigest != null) count++;
        return count;
    }
}
