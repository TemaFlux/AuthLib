package com.mojang.authlib.yggdrasil;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pro.gravit.launcher.request.auth.CheckServerRequest;
import pro.gravit.launcher.request.auth.JoinServerRequest;
import pro.gravit.utils.helper.CommonHelper;
import pro.gravit.utils.helper.IOHelper;

@Deprecated
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LegacyBridge {
    private static final Logger logger = LogManager.getLogger(LegacyBridge.class);

    public static boolean checkServer(String username, String serverID) {
        return new CheckServerRequest(username, serverID).request() != null;
    }

    public static String getCloakURL(String username) {
        return CommonHelper.replace(System.getProperty(
            "launcher.legacy.cloaksURL", "https://skins.minecraft.net/MinecraftCloaks/%username%.png"
        ), "username", IOHelper.urlEncode(username));
    }

    public static String getSkinURL(String username) {
        return CommonHelper.replace(System.getProperty(
            "launcher.legacy.skinsURL", "https://skins.minecraft.net/MinecraftSkins/%username%.png"
        ), "username", IOHelper.urlEncode(username));
    }

    public static String joinServer(String username, String accessToken, String serverID) {
        try {
            return new JoinServerRequest(username, accessToken, serverID).request().allow ? "OK" : "Bad Login (Clientside)";
        } catch (Exception e) {
            return e.toString();
        }
    }
}
