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

package au.com.grieve.geyserlink;

import au.com.grieve.geyserlink.config.Configuration;
import au.com.grieve.geyserlink.config.Dynamic;
import au.com.grieve.geyserlink.message.messages.GeyserLinkMessage;
import au.com.grieve.geyserlink.message.messages.PingMessage;
import au.com.grieve.geyserlink.message.messages.WhoisMessage;
import au.com.grieve.geyserlink.message.messages.WrappedMessage;
import au.com.grieve.geyserlink.message.responses.GeyserLinkResponse;
import au.com.grieve.geyserlink.message.responses.PingResponse;
import au.com.grieve.geyserlink.message.responses.WhoisResponse;
import au.com.grieve.geyserlink.message.responses.WrappedResponse;
import au.com.grieve.geyserlink.message.wrappers.GeyserLinkSignedMessage;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Getter
public class GeyserLink {
    private static GeyserLink INSTANCE;

    private final AtomicInteger seq = new AtomicInteger();
    private final Map<Integer, MessageResult.ResponseRunnable> responseMap = new ConcurrentHashMap<>();

    private final IGeyserLinkPlatform platform;
    private final File configFile;
    private final File dynamicFile;
    private final Map<UUID, PublicKey> knownKeys = new HashMap<>();
    private final Map<UUID, PublicKey> trustedKeys = new HashMap<>();
    private Configuration config;
    private Dynamic dynamic;
    private KeyPair keyPair;
    private UUID myUUID;


    public GeyserLink(IGeyserLinkPlatform platform) {
        INSTANCE = this;
        this.platform = platform;
        this.configFile = new File(platform.getDataFolder(), "config.yml");
        this.dynamicFile = new File(platform.getDataFolder(), "dynamic.yml");

        // Load Configuation
        loadConfig();

        // Load Keys
        loadKeys();
    }

    public static GeyserLink getInstance() {
        return INSTANCE;
    }

    protected void loadConfig() {
        // Setup Configuration
        if (!configFile.exists()) {
            generateConfig();
        }
        config = Configuration.loadFromFile(configFile);
        dynamic = Dynamic.loadFromFile(dynamicFile);

    }

    protected void loadKeys() {
        knownKeys.clear();
        trustedKeys.clear();

        // If we have no public key we generate it now
        if (dynamic.getConfig() == null || dynamic.getConfig().getPrivateKey() == null) {
            platform.getLogger().info("Generating new Public/Private Key");
            keyPair = EncryptionUtils.generateKeyPair();
            if (keyPair != null) {
                dynamic.getConfig().setPublicKey(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
                dynamic.getConfig().setPrivateKey(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
                dynamic.saveToFile(dynamicFile);
            }
        } else {
            keyPair = EncryptionUtils.generateKeyPair(
                    Base64.getDecoder().decode(dynamic.getConfig().getPublicKey()),
                    Base64.getDecoder().decode(dynamic.getConfig().getPrivateKey()));
        }

        if (keyPair != null) {
            myUUID = EncryptionUtils.toUUID(keyPair.getPublic());
            knownKeys.put(myUUID, keyPair.getPublic());
            trustedKeys.put(myUUID, keyPair.getPublic());

        }

        // Load Known and Trusted keys
        for (String publicKeyString : dynamic.getTrusted().values()) {
            PublicKey publicKey = EncryptionUtils.byteArrayToPublicKey(Base64.getDecoder().decode(publicKeyString));
            if (publicKey != null) {
                trustedKeys.put(EncryptionUtils.toUUID(publicKey), publicKey);
                knownKeys.put(EncryptionUtils.toUUID(publicKey), publicKey);
            }
        }

        for (String publicKeyString : dynamic.getKnown().values()) {
            PublicKey publicKey = EncryptionUtils.byteArrayToPublicKey(Base64.getDecoder().decode(publicKeyString));
            if (publicKey != null) {
                knownKeys.put(EncryptionUtils.toUUID(publicKey), publicKey);
            }
        }
    }

    protected void saveKeys() {
        dynamic.getTrusted().clear();
        dynamic.getKnown().clear();

        for (PublicKey key : trustedKeys.values()) {
            dynamic.getTrusted().put(EncryptionUtils.toUUID(key).toString(), Base64.getEncoder().encodeToString(key.getEncoded()));
        }

        for (Map.Entry<UUID, PublicKey> entry : knownKeys.entrySet().stream().filter(e -> !trustedKeys.containsKey(e.getKey())).collect(Collectors.toList())) {
            dynamic.getKnown().put(EncryptionUtils.toUUID(entry.getValue()).toString(), Base64.getEncoder().encodeToString(entry.getValue().getEncoded()));
        }
        dynamic.saveToFile(dynamicFile);
    }

    /**
     * Generate new configuration file
     */
    protected void generateConfig() {
        //noinspection ResultOfMethodCallIgnored
        configFile.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(configFile);
             InputStream fis = platform.getPlatformResourceAsStream("config.yml")) {
            fis.transferTo(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send Raw GeyserLink Signed Message
     */
    public MessageResult sendMessage(Object recipient, String channel, String subChannel, byte[] data) {
        GeyserLinkMessage message = new GeyserLinkMessage(
                seq.incrementAndGet(),
                EncryptionUtils.toUUID(keyPair.getPublic()),
                channel,
                subChannel,
                data
        );

        GeyserLinkSignedMessage<GeyserLinkMessage> signedMessage = GeyserLinkSignedMessage.createSignedMessage(message, keyPair.getPrivate(), GeyserLinkMessage.class);
        platform.sendPluginMessage(recipient, "geyserlink:message", signedMessage);
        return new MessageResult(this, recipient, message);
    }

    /**
     * Send a Wrapped Message Object
     */
    public MessageResult sendMessage(Object recipient, WrappedMessage message) {
        return sendMessage(recipient, message.getChannel(), message.getSubChannel(), message.getBytes());
    }

    /**
     * Send GeyserLink Response Message
     */
    public void sendResponse(Object recipient, GeyserLinkMessage message, byte[] data) {
        GeyserLinkResponse response = new GeyserLinkResponse(
                message.getId(),
                EncryptionUtils.toUUID(keyPair.getPublic()),
                message.getSender(),
                data
        );
        GeyserLinkSignedMessage<GeyserLinkResponse> signedMessage = GeyserLinkSignedMessage.createSignedMessage(response, keyPair.getPrivate(), GeyserLinkResponse.class);
        platform.sendPluginMessage(recipient, "geyserlink:response", signedMessage);
    }

    public void sendResponse(Object recipient, GeyserLinkMessage message, WrappedResponse response) {
        sendResponse(recipient, message, response.getBytes());
    }

    /**
     * Built in Messages
     */
    public void handleMainMessage(Object sender, GeyserLinkSignedMessage<GeyserLinkMessage> message) {
        try {
            // If Message is not known we will send a whois request out to get the public key
            if (!message.isKnown()) {
                sendMessage(sender, new WhoisMessage(message.getMessage().getSender()))
                        .onResponse(WhoisResponse.class, (result, signed, response) -> {
                            if (!signed.getMessage().getSender().equals(message.getMessage().getSender())) {
                                return;
                            }

                            if (!signed.isValid(response.getPublicKey())) {
                                getPlatform().getLogger().debug("WhoisResponse is not signed properly");
                                return;
                            }

                            getKnownKeys().put(signed.getMessage().getSender(), response.getPublicKey());
                            saveKeys();
                        });
            }

            switch (message.getMessage().getSubChannel()) {
                case "ping":  // Return a ping message containing the data payload
                    PingMessage pingMessage = new PingMessage(PingMessage.from(message.getMessage().getPayload()));
                    sendResponse(sender, message.getMessage(), new PingResponse(pingMessage.getData()));
                    break;
                case "whois": // Return our public key if someone wants it
                    WhoisMessage whoisMessage = new WhoisMessage(WhoisMessage.from(message.getMessage().getPayload()));
                    if (whoisMessage.getUuid().equals(GeyserLink.getInstance().getMyUUID())) {
                        sendResponse(sender, message.getMessage(), new WhoisResponse(GeyserLink.getInstance().keyPair.getPublic()));
                    }
                    break;
            }
        } catch (IOException ignored) {
        }
    }

    /**
     * Handle Responses
     */
    public void handleResponse(Object sender, GeyserLinkSignedMessage<GeyserLinkResponse> message) {
        if (!message.getMessage().getRecipient().equals(myUUID)) {
            return;
        }

        MessageResult.ResponseRunnable runnable = responseMap.get(message.getMessage().getId());
        if (runnable != null) {
            runnable.run(message);
        }
    }

    @SuppressWarnings({"SameParameterValue", "UnusedReturnValue", "unused"})
    @Getter
    @RequiredArgsConstructor
    public static class MessageResult {
        private final GeyserLink geyserLink;
        private final Object recipient;
        private final GeyserLinkMessage message;

        public <T extends GeyserLinkSignedMessage<GeyserLinkResponse>> MessageResult onResponse(Runnable runnable, long timeout, long expiry) {
            // Clean up after a timeout
            IScheduledTask timeoutTask = geyserLink.getPlatform().schedule(() -> geyserLink.getResponseMap().remove(message.getId()), timeout);

            geyserLink.getResponseMap().put(message.getId(), response -> {
                // Cancel cleanup
                timeoutTask.cancel();

                // Set expiry
                geyserLink.getPlatform().schedule(() -> geyserLink.getResponseMap().remove(message.getId()), expiry);

                // Execute
                runnable.run(MessageResult.this, response);
            });

            return this;
        }

        public <T extends GeyserLinkSignedMessage<GeyserLinkResponse>> MessageResult onResponse(Runnable runnable) {
            return onResponse(runnable, 300, 30);
        }

        public <T extends WrappedResponse> MessageResult onResponse(Class<T> responseClass, WrappedRunnable<T> runnable, long timeout, long expiry) {
            return onResponse((result, response) -> {
                try {
                    runnable.run(
                            result,
                            response,
                            responseClass.getConstructor(JsonNode.class)
                                    .newInstance(WrappedResponse.from(response.getMessage().getPayload())));
                } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException e) {
                    e.printStackTrace();
                }
            }, timeout, expiry);
        }

        // Wrapped Response
        public <T extends WrappedResponse> MessageResult onResponse(Class<T> responseClass, WrappedRunnable<T> runnable) {
            return onResponse(responseClass, runnable, 300, 30);
        }

        public interface Runnable {
            void run(MessageResult result, GeyserLinkSignedMessage<GeyserLinkResponse> signed);
        }

        public interface WrappedRunnable<T extends WrappedResponse> {
            void run(MessageResult result, GeyserLinkSignedMessage<GeyserLinkResponse> signed, T response);
        }

        public interface ResponseRunnable {
            void run(GeyserLinkSignedMessage<GeyserLinkResponse> response);
        }
    }
}
