package com.mojang.authlib.yggdrasil;

import com.google.common.collect.Iterables;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.minecraft.BaseMinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pro.gravit.launcher.profiles.PlayerProfile;
import pro.gravit.launcher.profiles.Texture;
import pro.gravit.launcher.request.auth.CheckServerRequest;
import pro.gravit.launcher.request.auth.JoinServerRequest;
import pro.gravit.launcher.request.uuid.ProfileByUUIDRequest;
import pro.gravit.utils.helper.SecurityHelper;

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class YggdrasilMinecraftSessionService
extends BaseMinecraftSessionService {
    public static final boolean NO_TEXTURES = Boolean.getBoolean("launcher.com.mojang.authlib.noTextures");
    private static final Type TEXTURES_TYPE_TOKEN = new TypeToken<Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>>() {}.getType();
    private static final Gson GSON = new GsonBuilder().create();
    private static final Logger logger = LogManager.getLogger(YggdrasilMinecraftSessionService.class);

    public YggdrasilMinecraftSessionService(AuthenticationService service) {
        super(service);
        logger.debug("Patched MinecraftSessionService created");
    }

    public YggdrasilMinecraftSessionService(YggdrasilAuthenticationService service) {
        super(service);
        logger.debug("Patched MinecraftSessionService created");
    }

    public static void fillTextureProperties(GameProfile profile, PlayerProfile pp) {
        logger.debug("fillTextureProperties, Username: '" + profile.getName() + "'");
        if (NO_TEXTURES) return;

        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = new HashMap<>();
        try {
            if (pp.assets != null) {
                for (MinecraftProfileTexture.Type type : MinecraftProfileTexture.Type.values()) {
                    Texture texture = pp.assets.get(type.name());

                    if (texture == null) continue;
                    textures.put(type, YggdrasilMinecraftSessionService.makeMinecraftTexture(texture));
                }
            }
        } catch (Throwable ignored) {}

        MinecraftTexturesProperty payload = new MinecraftTexturesProperty();
        payload.textures = textures;
        payload.isPublic = true;
        payload.profileId = pp.uuid.toString().replace("-", "");
        payload.profileName = pp.username;

        String serializedData = Base64.getEncoder().encodeToString(GSON.toJson(payload).getBytes(StandardCharsets.UTF_8));
        logger.debug("Write textures " + serializedData);

        profile.getProperties().put("textures", new Property("textures", serializedData, ""));
    }

    private static MinecraftProfileTexture makeMinecraftTexture(Texture texture) {
        return new MinecraftProfileTexture(texture.url, SecurityHelper.toHex(texture.digest), texture.metadata);
    }

    public static GameProfile toGameProfile(PlayerProfile pp) {
        GameProfile profile = new GameProfile(pp.uuid, pp.username);
        try {
            if (pp.properties != null) {
                PropertyMap propertyMap = profile.getProperties();
                for (Map.Entry<String, String> prop : pp.properties.entrySet()) {
                    propertyMap.put(prop.getKey(), new Property(prop.getKey(), prop.getValue(), ""));
                }
            }
        } catch (Throwable ignored) {}

        fillTextureProperties(profile, pp);
        return profile;
    }

    @Override public GameProfile fillProfileProperties(GameProfile profile, boolean requireSecure) {
        UUID uuid = profile.getId();

        logger.debug("fillProfileProperties, UUID: " + uuid);
        if (uuid == null) return profile;

        PlayerProfile pp; try {
            pp = new ProfileByUUIDRequest(uuid).request().playerProfile;
        } catch (Exception e) {
            logger.warn("Couldn't fetch profile properties for '{}': {}", profile, e);
            return profile;
        }

        if (pp == null) {
            logger.warn("Couldn't fetch profile properties for '{}' as the profile does not exist", profile);
            return profile;
        }

        logger.debug("Successfully fetched profile properties for '" + profile + "'");
        fillTextureProperties(profile, pp);
        return toGameProfile(pp);
    }

    @Override public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getTextures(GameProfile profile, boolean requireSecure) {
        logger.debug("getTextures, Username: '" + profile.getName() + "', UUID: '" + profile.getId() + "'");

        EnumMap<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = new EnumMap<>(MinecraftProfileTexture.Type.class);
        if (!NO_TEXTURES) {
            try {
                Property texturesProperty = Iterables.getFirst(profile.getProperties().get("textures"), null);
                if (texturesProperty != null) {
                    logger.trace("Read textures property " + texturesProperty.getValue());

                    String serializedData = new String(Base64.getDecoder().decode(texturesProperty.getValue()), StandardCharsets.UTF_8);
                    MinecraftTexturesProperty payload = GSON.fromJson(serializedData, MinecraftTexturesProperty.class);
                    textures.putAll(payload.textures);
                }
            } catch (Throwable e) {
                logger.error(e);
            }
        }

        for (Map.Entry<MinecraftProfileTexture.Type, MinecraftProfileTexture> entry : textures.entrySet())
            logger.trace("Found " + entry.getKey() + ": " + entry.getValue().getUrl());

        logger.trace("Found " + textures.size() + " textures");
        return textures;
    }

    @Override public GameProfile hasJoinedServer(GameProfile profile, String serverID) throws AuthenticationUnavailableException {
        String username = profile.getName();
        logger.debug("checkServer, Username: '" + username + "', Server ID: " + serverID);

        PlayerProfile pp; try {
            pp = new CheckServerRequest(username, serverID).request().playerProfile;
        } catch (Exception e) {
            logger.error(e);
            throw new AuthenticationUnavailableException(e);
        }

        return pp == null ? null : YggdrasilMinecraftSessionService.toGameProfile(pp);
    }

    @Override public GameProfile hasJoinedServer(GameProfile profile, String serverID, InetAddress address) throws AuthenticationUnavailableException {
        return hasJoinedServer(profile, serverID);
    }

    @Override public YggdrasilAuthenticationService getAuthenticationService() {
        return (YggdrasilAuthenticationService) super.getAuthenticationService();
    }

    @Override public void joinServer(GameProfile profile, String accessToken, String serverID) throws AuthenticationException {
        String username = profile.getName();
        logger.debug("joinServer, Username: '" + username + "', Access token: " + accessToken + ", Server ID: " + serverID);

        boolean success; try {
            success = new JoinServerRequest(username, accessToken, serverID).request().allow;
        } catch (Exception e) {
            logger.error(e);
            throw new AuthenticationUnavailableException(e);
        }

        if (!success) throw new AuthenticationException("Failed to verify username (Clientside). Please restart the launcher and client");
    }

    @NoArgsConstructor @AllArgsConstructor
    public static class MinecraftTexturesProperty {
        public long timestamp;
        public String profileId, profileName;
        public boolean isPublic;
        public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures;
    }
}
