package com.mojang.authlib;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.util.UUIDTypeAdapter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseUserAuthentication
implements UserAuthentication {
    private static final Logger LOGGER = LogManager.getLogger();

    protected static final String STORAGE_KEY_PROFILE_NAME = "displayName";
    protected static final String STORAGE_KEY_PROFILE_ID = "uuid";
    protected static final String STORAGE_KEY_PROFILE_PROPERTIES = "profileProperties";
    protected static final String STORAGE_KEY_USER_NAME = "username";
    protected static final String STORAGE_KEY_USER_ID = "userid";
    protected static final String STORAGE_KEY_USER_PROPERTIES = "userProperties";

    @Getter private final AuthenticationService authenticationService;
    private final PropertyMap userProperties = new PropertyMap();
    @Setter(AccessLevel.PROTECTED) private String userid;
    @Getter(AccessLevel.PROTECTED) private String username, password;
    @Getter @Setter(AccessLevel.PROTECTED) private GameProfile selectedProfile;
    @Setter(AccessLevel.PROTECTED) private UserType userType;

    protected BaseUserAuthentication(AuthenticationService authenticationService) {
        Validate.notNull(authenticationService);
        this.authenticationService = authenticationService;
    }

    @Override public boolean canLogIn() {
        return !canPlayOnline() &&
               StringUtils.isNotBlank(getUsername()) &&
               StringUtils.isNotBlank(getPassword());
    }

    @Override public void logOut() {
        password = null;
        userid = null;
        setSelectedProfile(null);
        getModifiableUserProperties().clear();
        setUserType(null);
    }

    @Override public boolean isLoggedIn() {
        return getSelectedProfile() != null;
    }

    @Override public void setUsername(String username) {
        if (isLoggedIn() && canPlayOnline()) throw new IllegalStateException("Cannot change username whilst logged in & online");
        this.username = username;
    }

    @Override public void setPassword(String password) {
        if (isLoggedIn() && canPlayOnline() && StringUtils.isNotBlank(password)) throw new IllegalStateException("Cannot set password whilst logged in & online");
        this.password = password;
    }

    @SuppressWarnings("unchecked cast")
    @Override public void loadFromStorage(Map<String, Object> credentials) {
        logOut();
        setUsername(String.valueOf(credentials.get(STORAGE_KEY_USER_NAME)));
        userid = credentials.containsKey(STORAGE_KEY_USER_ID) ? String.valueOf(credentials.get(STORAGE_KEY_USER_ID)) : username;

        if (credentials.containsKey(STORAGE_KEY_USER_PROPERTIES)) {
            try {
                List<Map<String, String>> list = (List<Map<String, String>>) credentials.get(STORAGE_KEY_USER_PROPERTIES);
                for (Map<String, String> propertyMap : list) {
                    String name = propertyMap.get("name"), value = propertyMap.get("value"), signature = propertyMap.get("signature");
                    getModifiableUserProperties().put(name, signature == null ?
                        new Property(name, value) :
                        new Property(name, value, signature)
                    );
                }
            } catch (Throwable t) {
                LOGGER.warn("Couldn't deserialize user properties", t);
            }
        }

        if (credentials.containsKey(STORAGE_KEY_PROFILE_NAME) && credentials.containsKey(STORAGE_KEY_PROFILE_ID)) {
            GameProfile profile = new GameProfile(UUIDTypeAdapter.fromString(
                String.valueOf(credentials.get(STORAGE_KEY_PROFILE_ID))),
                String.valueOf(credentials.get(STORAGE_KEY_PROFILE_NAME))
            );

            if (credentials.containsKey(STORAGE_KEY_PROFILE_PROPERTIES)) {
                try {
                    List<Map<String, String>> list = (List<Map<String, String>>) credentials.get(STORAGE_KEY_PROFILE_PROPERTIES);
                    for (Map<String, String> propertyMap : list) {
                        String name = propertyMap.get("name"), value = propertyMap.get("value"), signature = propertyMap.get("signature");
                        getModifiableUserProperties().put(name, signature == null ?
                            new Property(name, value) :
                            new Property(name, value, signature)
                        );
                    }
                } catch (Throwable t) {
                    LOGGER.warn("Couldn't deserialize profile properties", t);
                }
            }

            setSelectedProfile(profile);
        }
    }

    @Override public Map<String, Object> saveForStorage() {
        Map<String, Object> result = new HashMap<>();
        if (getUsername() != null) result.put(STORAGE_KEY_USER_NAME, getUsername());
        if (getUserID() != null) result.put(STORAGE_KEY_USER_ID, getUserID());
        else if (getUsername() != null) result.put(STORAGE_KEY_USER_NAME, getUsername());

        if (!getUserProperties().isEmpty()) {
            List<Map<String, String>> properties = new ArrayList<>();

            for (Property userProperty : getUserProperties().values()) {
                Map<String, String> property = new HashMap<>();
                property.put("name", userProperty.getName());
                property.put("value", userProperty.getValue());
                property.put("signature", userProperty.getSignature());
                properties.add(property);
            }

            result.put(STORAGE_KEY_USER_PROPERTIES, properties);
        }

        GameProfile selectedProfile = getSelectedProfile();
        if (selectedProfile != null) {
            result.put(STORAGE_KEY_PROFILE_NAME, selectedProfile.getName());
            result.put(STORAGE_KEY_PROFILE_ID, selectedProfile.getId());

            List<Map<String, String>> properties = new ArrayList<>();
            for (Property profileProperty : selectedProfile.getProperties().values()) {
                Map<String, String> property = new HashMap<>();
                property.put("name", profileProperty.getName());
                property.put("value", profileProperty.getValue());
                property.put("signature", profileProperty.getSignature());
                properties.add(property);
            }

            if (!properties.isEmpty()) result.put(STORAGE_KEY_PROFILE_PROPERTIES, properties);
        }

        return result;
    }

    @Override public String toString() {
        StringBuilder result = new StringBuilder()
        .append(getClass().getSimpleName()).append("{");

        if (isLoggedIn()) {
            result.append("Logged in as ").append(getUsername());
            if (getSelectedProfile() != null) result.append(" / ")
                    .append(getSelectedProfile()).append(" - ")
                    .append(canPlayOnline() ? "Online" : "Offline");
        } else result.append("Not logged in");

        return result.append("}").toString();
    }

    @Override public PropertyMap getUserProperties() {
        if (isLoggedIn()) {
            PropertyMap result = new PropertyMap();
            result.putAll(getModifiableUserProperties());
            return result;
        }

        return new PropertyMap();
    }

    protected PropertyMap getModifiableUserProperties() {
        return userProperties;
    }

    @Override public UserType getUserType() {
        return isLoggedIn() ? (userType == null ? UserType.LEGACY : userType) : null;
    }

    @Override public String getUserID() {
        return userid;
    }
}
