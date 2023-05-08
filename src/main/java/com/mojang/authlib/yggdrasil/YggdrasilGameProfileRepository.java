package com.mojang.authlib.yggdrasil;

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pro.gravit.launcher.profiles.PlayerProfile;
import pro.gravit.launcher.request.uuid.BatchProfileByUsernameRequest;
import pro.gravit.utils.helper.VerifyHelper;

import java.util.Arrays;

public class YggdrasilGameProfileRepository
implements GameProfileRepository {
    private static final Logger logger = LogManager.getLogger(YggdrasilGameProfileRepository.class);

    private static final long BUSY_WAIT_MS = VerifyHelper.verifyLong(
        Long.parseLong(
            System.getProperty("launcher.com.mojang.authlib.busyWait", Long.toString(100L))
        ), VerifyHelper.L_NOT_NEGATIVE, "launcher.com.mojang.authlib.busyWait can't be < 0"
    ), ERROR_BUSY_WAIT_MS = VerifyHelper.verifyLong(
        Long.parseLong(
            System.getProperty("launcher.com.mojang.authlib.errorBusyWait", Long.toString(500L))
        ), VerifyHelper.L_NOT_NEGATIVE, "launcher.com.mojang.authlib.errorBusyWait can't be < 0"
    );

    public YggdrasilGameProfileRepository() {
        logger.debug("Patched GameProfileRepository created");
    }

    public YggdrasilGameProfileRepository(YggdrasilAuthenticationService ignored) {
        logger.debug("Patched GameProfileRepository created");
    }

    private static void busyWait(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }

    @Override public void findProfilesByNames(String[] usernames, Agent agent, ProfileLookupCallback callback) {
        int offset = 0;
        while (offset < usernames.length) {
            String[] sliceUsernames = Arrays.copyOfRange(usernames, offset, Math.min(offset + CompatBridge.PROFILES_MAX_BATCH_SIZE, usernames.length));
            offset += CompatBridge.PROFILES_MAX_BATCH_SIZE;

            PlayerProfile[] sliceProfiles; try {
                sliceProfiles = new BatchProfileByUsernameRequest(sliceUsernames).request().playerProfiles;
            } catch (Exception e) {
                for (String username : sliceUsernames) {
                    logger.warn("Couldn't find profile '" + username + "': " + e);
                    callback.onProfileLookupFailed(new GameProfile(null, username), e);
                }

                busyWait(ERROR_BUSY_WAIT_MS);
                continue;
            }

            int len = sliceProfiles.length; for (int i = 0; i < len; i++) {
                PlayerProfile pp = sliceProfiles[i];
                if (pp == null) {
                    String username = sliceUsernames[i];
                    logger.warn("Couldn't find profile '{}'", username);

                    callback.onProfileLookupFailed(
                        new GameProfile(null, username),
                        new ProfileNotFoundException("Server did not find the requested profile")
                    ); continue;
                }

                logger.debug("Successfully looked up profile '" + pp.username + "'");
                callback.onProfileLookupSucceeded(YggdrasilMinecraftSessionService.toGameProfile(pp));
            }

            busyWait(BUSY_WAIT_MS);
        }
    }
}
