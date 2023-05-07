package com.mojang.authlib.yggdrasil.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter(AccessLevel.PROTECTED)
public class Response {
    private String error, errorMessage, cause;
}
