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

package au.com.grieve.geyserlink.platform.spigot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Iterables;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


@Getter
public final class GeyserLink extends JavaPlugin implements Listener, PluginMessageListener {

    private static GeyserLink instance;

    private final File configFile = new File(getDataFolder(), "config.yml");
    private Configuration localConfig;

    private int upto = 0;

    public GeyserLink() {
        super();
        instance = this;
    }

    @Override
    public void onEnable() {
        // Setup Configuration
        if (!configFile.exists()) {
            generateConfig();
        }
        loadConfig();

        // Register Channels
        getServer().getMessenger().registerOutgoingPluginChannel(this, "geyserlink:main");
        getServer().getMessenger().registerIncomingPluginChannel(this, "geyserlink:main", this);

        getServer().getScheduler().runTaskTimer(this, () -> {
            Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
            if (player == null) {
                return;
            }

            getLogger().warning("Sending ping to player");

            player.sendPluginMessage(this, "geyserlink:main", String.format("From Spigot: %d", upto++).getBytes());

        }, 10, 200);

    }

    /**
     * Generate new configuration file
     */
    protected void generateConfig() {
        //noinspection ResultOfMethodCallIgnored
        configFile.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(configFile);
             InputStream fis = getResource("platform/spigot/config.yml")) {
            if (fis != null) {
                fis.transferTo(fos);
            }
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

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] bytes) {
        getLogger().warning("Message[" + channel + "]: " + new String(bytes));
    }
}
