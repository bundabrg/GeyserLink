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

package au.com.grieve.geyserlink.platform.bungeecord;

import au.com.grieve.geyserlink.models.GeyserLinkMessage;
import au.com.grieve.geyserlink.models.GeyserLinkResponse;
import au.com.grieve.geyserlink.platform.bungeecord.listeners.MessageListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Iterables;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class GeyserLink extends Plugin {
    public static final String SOURCE = "bungeecord";
    public static GeyserLink INSTANCE;
    private final File configFile = new File(getDataFolder(), "config.yml");
    private Configuration localConfig;

    private final AtomicInteger seq = new AtomicInteger();
    private final Map<Integer, MessageResult.ResponseRunnable> responseMap = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        super.onEnable();

        INSTANCE = this;

        // Setup Configuration
        if (!configFile.exists()) {
            generateConfig();
        }
        loadConfig();

        // Register Channels
        getProxy().getPluginManager().registerListener(this, new MessageListener(this));
        getProxy().getScheduler().schedule(this, () -> {

            ProxiedPlayer player = Iterables.getFirst(getProxy().getPlayers(), null);
            if (player == null) {
                return;
            }

            getLogger().warning("Sending ping to player");

            sendMessage(
                    player,
                    "geyserlink:main",
                    "ping",
                    new byte[]{(byte) seq.get()}
            ).onResponse((result, response) -> {
                getLogger().warning("Got a ping response: " + response);
            });

        }, 5, 5, TimeUnit.SECONDS);
    }

    /**
     * Generate new configuration file
     */
    protected void generateConfig() {
        //noinspection ResultOfMethodCallIgnored
        configFile.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(configFile);
             InputStream fis = getResourceAsStream("platform/bungeecord/config.yml")) {
            fis.transferTo(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load Configuration
     */
    protected void loadConfig() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            localConfig = mapper.readValue(configFile, Configuration.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a GeyserLink Message
     */
    public MessageResult sendMessage(Connection connection, String channel, String subChannel, byte[] data) {
        GeyserLinkMessage message = new GeyserLinkMessage(
                seq.incrementAndGet(),
                SOURCE,
                channel,
                subChannel,
                data,
                ""
        );
        if (connection instanceof ProxiedPlayer) {
            ((ProxiedPlayer) connection).sendData("geyserlink:message", message.getBytes());
        }

        if (connection instanceof Server) {
            ((Server) connection).getInfo().sendData("geyserlink:message", message.getBytes());
        }
        return new MessageResult(this, connection, message);
    }

    /**
     * Create a GeyserLink Response
     */
    public void sendResponse(Connection connection, GeyserLinkMessage message, byte[] data) {
        GeyserLinkResponse response = new GeyserLinkResponse(
                message.getId(),
                SOURCE,
                message.getSource(),
                data,
                ""
        );

        if (connection instanceof ProxiedPlayer) {
            ((ProxiedPlayer) connection).sendData("geyserlink:response", response.getBytes());
        }

        if (connection instanceof Server) {
            ((Server) connection).getInfo().sendData("geyserlink:response", response.getBytes());
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class MessageResult {
        private final GeyserLink plugin;
        private final Connection connection;
        private final GeyserLinkMessage message;

        MessageResult onResponse(Runnable runnable, long timeout, long expiry) {
            // Clean up after a timeout
            ScheduledTask timeoutTask = plugin.getProxy().getScheduler().schedule(plugin, () -> plugin.getResponseMap().remove(message.getId()), timeout, TimeUnit.SECONDS);

            plugin.getResponseMap().put(message.getId(), (response) -> {
                // Cancel cleanup
                timeoutTask.cancel();

                // Set expiry
                plugin.getProxy().getScheduler().schedule(plugin, () -> plugin.getResponseMap().remove(message.getId()), expiry, TimeUnit.SECONDS);

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
