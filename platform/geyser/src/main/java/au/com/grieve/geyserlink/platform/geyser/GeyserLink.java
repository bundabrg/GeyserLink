/*
 * GeyserLink - The Missing Link
 * Copyright (C) 2020 GeyserLink Developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package au.com.grieve.geyserlink.platform.geyser;

import au.com.grieve.geyserlink.EncryptionUtils;
import au.com.grieve.geyserlink.config.Configuration;
import au.com.grieve.geyserlink.config.Dynamic;
import au.com.grieve.geyserlink.models.GeyserLinkMessage;
import au.com.grieve.geyserlink.models.GeyserLinkResponse;
import au.com.grieve.geyserlink.models.GeyserLinkSignedMessage;
import au.com.grieve.geyserlink.platform.geyser.listeners.MessageListener;
import com.google.common.collect.Iterables;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.geysermc.connector.event.annotations.Event;
import org.geysermc.connector.event.events.PluginEnableEvent;
import org.geysermc.connector.network.session.GeyserSession;
import org.geysermc.connector.plugin.GeyserPlugin;
import org.geysermc.connector.plugin.PluginClassLoader;
import org.geysermc.connector.plugin.PluginManager;
import org.geysermc.connector.plugin.annotations.Plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
@Plugin(
        name = "GeyserLink",
        version = "1.1.0-dev",
        authors = {"Bundabrg"},
        description = "The Missing Link"
)
@Getter
public class GeyserLink extends GeyserPlugin {
    public static GeyserLink INSTANCE;
    private final File configFile = new File(getDataFolder(), "config.yml");
    private final File dynamicFile = new File(getDataFolder(), "dynamic.yml");
    private Configuration localConfig;
    private final AtomicInteger seq = new AtomicInteger();
    private Dynamic dynamicConfig;
    private KeyPair keyPair;
    private final Map<Integer, MessageResult.ResponseRunnable> responseMap = new ConcurrentHashMap<>();

    public GeyserLink(PluginManager pluginManager, PluginClassLoader pluginClassLoader) {
        super(pluginManager, pluginClassLoader);

        INSTANCE = this;
    }

    @Event
    public void onEnable(PluginEnableEvent event) {
        if (event.getPlugin() == this) {
            // Setup Configuration
            if (!configFile.exists()) {
                generateConfig();
            }
            localConfig = Configuration.loadFromFile(configFile);
            dynamicConfig = Dynamic.loadFromFile(dynamicFile);

            // If we have no public key we generate it now
            if (dynamicConfig.getConfig() == null || dynamicConfig.getConfig().getPrivateKey() == null) {
                getLogger().info("Generating new Public/Private Key");
                keyPair = EncryptionUtils.generateKeyPair();
                if (keyPair != null) {
                    dynamicConfig.getConfig().setPublicKey(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
                    dynamicConfig.getConfig().setPrivateKey(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
                    dynamicConfig.saveToFile(dynamicFile);
                }
            } else {
                keyPair = EncryptionUtils.generateKeyPair(
                        Base64.getDecoder().decode(dynamicConfig.getConfig().getPublicKey()),
                        Base64.getDecoder().decode(dynamicConfig.getConfig().getPrivateKey()));
            }

            // Register Listeners
            registerEvents(new MessageListener(this));

            getConnector().getGeneralThreadPool().scheduleAtFixedRate(() -> {
                GeyserSession session = Iterables.getFirst(getConnector().getPlayers().values(), null);

                if (session == null) {
                    return;
                }

                getLogger().warning("Sending ping to player");
                sendMessage(
                        session,
                        "geyserlink:main",
                        "ping",
                        new byte[]{(byte) seq.get()}
                ).onResponse((result, response) -> {
                    getLogger().warning("Got a ping response: " + response);
                });

            }, 5, 5, TimeUnit.SECONDS);
        }
    }

    /**
     * Generate new configuration file
     */
    protected void generateConfig() {
        //noinspection ResultOfMethodCallIgnored
        configFile.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(configFile);
             InputStream fis = getResourceAsStream("platform/geyser/config.yml")) {
            fis.transferTo(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public UUID getUuid() {
        return UUID.nameUUIDFromBytes(keyPair.getPublic().getEncoded());
    }

    /**
     * Send a GeyserLink Message
     */
    public MessageResult sendMessage(GeyserSession session, String channel, String subChannel, byte[] data) {
        GeyserLinkMessage message = new GeyserLinkMessage(
                seq.incrementAndGet(),
                getUuid(),
                channel,
                subChannel,
                data
        );
        session.sendPluginMessage("geyserlink:message", GeyserLinkSignedMessage.sign(message, keyPair.getPrivate()).getBytes());
        return new MessageResult(this, session, message);
    }

    /**
     * Send a GeyserLink Response
     */
    public void sendResponse(GeyserSession session, GeyserLinkMessage message, byte[] data) {
        GeyserLinkResponse response = new GeyserLinkResponse(
                message.getId(),
                getUuid(),
                message.getSender(),
                data
        );
        session.sendPluginMessage("geyserlink:response", GeyserLinkSignedMessage.sign(response, keyPair.getPrivate()).getBytes());
    }

    @SuppressWarnings({"SameParameterValue", "UnusedReturnValue"})
    @Getter
    @RequiredArgsConstructor
    public static class MessageResult {
        private final GeyserLink plugin;
        private final GeyserSession session;
        private final GeyserLinkMessage message;

        MessageResult onResponse(Runnable runnable, long timeout, long expiry) {
            // Clean up after a timeout
            ScheduledFuture<?> timeoutTask = plugin.getConnector().getGeneralThreadPool().schedule(() -> plugin.getResponseMap().remove(message.getId()), timeout, TimeUnit.SECONDS);

            plugin.getResponseMap().put(message.getId(), (response) -> {
                // Cancel cleanup
                timeoutTask.cancel(false);

                // Set expiry
                plugin.getConnector().getGeneralThreadPool().schedule(() -> plugin.getResponseMap().remove(message.getId()), expiry, TimeUnit.SECONDS);

                // Execute
                runnable.run(this, response);
            });

            return this;
        }

        MessageResult onResponse(Runnable runnable) {
            return onResponse(runnable, 300, 30);
        }

        public interface Runnable {
            void run(MessageResult result, GeyserLinkResponse response);
        }

        public interface ResponseRunnable {
            void run(GeyserLinkResponse response);
        }
    }
}
