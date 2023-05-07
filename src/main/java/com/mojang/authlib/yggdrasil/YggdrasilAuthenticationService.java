package com.mojang.authlib.yggdrasil;

import com.mojang.authlib.Agent;
import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import lombok.Getter;

import java.net.Proxy;
import java.net.URL;

public class YggdrasilAuthenticationService
implements AuthenticationService {
    // private static final Logger logger = LogManager.getLogger(YggdrasilMinecraftSessionService.class);
    @Getter private final String clientToken;

    public YggdrasilAuthenticationService(Proxy ignored, String clientToken) {
        this.clientToken = clientToken;
        // logger.debug("Patched AuthenticationService created: '{}'", clientToken); # Exception: org.apache.logging.log4j.Logger.debug(Ljava/lang/String;Ljava/lang/Object;)V
    }

    @Override public UserAuthentication createUserAuthentication(Agent agent) {
        throw new UnsupportedOperationException("createUserAuthentication is used only by Mojang Launcher");
    }

    @Override public MinecraftSessionService createMinecraftSessionService() {
        return new YggdrasilMinecraftSessionService(this);
    }

    @Override public GameProfileRepository createProfileRepository() {
        return new YggdrasilGameProfileRepository();
    }

    public <T> T makeRequest(URL routeRefresh, Object request, Class<T> aClass) throws AuthenticationException {
        try {
            return aClass.newInstance();
        } catch (Throwable e) {
            throw new AuthenticationException();
        }
    }
}
