package com.mojang.authlib.minecraft;

import java.util.Map;
import javax.annotation.Nullable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

@RequiredArgsConstructor
public class MinecraftProfileTexture {
    public static final int PROFILE_TEXTURE_COUNT = Type.values().length;

    @Getter private final String url;
    private final Map<String, String> metadata;

    @Nullable public String getMetadata(String key) {
        return metadata == null ? null : metadata.get(key);
    }

    public String getHash() {
        return FilenameUtils.getBaseName(url);
    }

    @Override public String toString() {
        return new ToStringBuilder(this)
        .append("url", url)
        .append("hash", getHash())
        .toString();
    }

    public enum Type { SKIN, CAPE, ELYTRA }
}
