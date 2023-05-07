package com.mojang.authlib;

import com.mojang.authlib.properties.PropertyMap;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

@Data
public class GameProfile {
    private final UUID id;
    private final String name;
    private final PropertyMap properties = new PropertyMap();
    private boolean legacy;

    public GameProfile(UUID id, String name) {
        if (id == null && StringUtils.isBlank(name)) throw new IllegalArgumentException("Name and ID cannot both be blank");
        this.id = id;
        this.name = name;
    }

    public boolean isComplete() {
        return id != null && StringUtils.isNotBlank(getName());
    }
}
