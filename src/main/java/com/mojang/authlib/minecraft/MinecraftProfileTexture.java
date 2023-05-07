package com.mojang.authlib.minecraft;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;

@RequiredArgsConstructor
public class MinecraftProfileTexture {
    public static final Set<Type> PROFILE_TEXTURE_TYPES = Collections.unmodifiableSet(EnumSet.allOf(Type.class));
    public static final int PROFILE_TEXTURE_COUNT = PROFILE_TEXTURE_TYPES.size();

    @Getter private final String url;
    private final String hash;
    private final Map<String, String> metadata;

    public MinecraftProfileTexture(String url) {
        this(url, baseName(url));
    }

    public MinecraftProfileTexture(String url, String hash) {
        this(url, hash, null);
    }

    @Nullable public String getMetadata(String key) {
        return metadata == null ? null : metadata.get(key);
    }

    private static String baseName(String url) {
        String name = url.substring(url.lastIndexOf(47) + 1);
        int extensionIndex = name.lastIndexOf(46);
        return extensionIndex >= 0 ? name.substring(0, extensionIndex) : name;
    }

    public String getHash() {
        return hash == null ? baseName(url) : hash;
    }

    @Override public String toString() {
        return new ToStringBuilder(this)
        .append("url", url)
        .append("hash", getHash())
        .toString();
    }

    public enum Type { SKIN, CAPE, ELYTRA }
}
