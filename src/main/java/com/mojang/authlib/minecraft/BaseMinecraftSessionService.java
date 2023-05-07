package com.mojang.authlib.minecraft;

import com.mojang.authlib.AuthenticationService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseMinecraftSessionService
implements MinecraftSessionService {
    private final AuthenticationService authenticationService;
}
