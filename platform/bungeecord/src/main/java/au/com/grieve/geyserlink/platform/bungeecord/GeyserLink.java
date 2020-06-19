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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Iterables;
import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Getter
public class GeyserLink extends Plugin implements Listener {
    private int upto = 0;
    private final File configFile = new File(getDataFolder(), "config.yml");
    private Configuration localConfig;

    @Override
    public void onEnable() {
        super.onEnable();

        // Setup Configuration
        if (!configFile.exists()) {
            generateConfig();
        }
        loadConfig();

        // Register Channels
        getProxy().registerChannel("geyerlink:main");
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getScheduler().schedule(this, () -> {

            ProxiedPlayer player = Iterables.getFirst(getProxy().getPlayers(), null);
            if (player == null) {
                return;
            }

            getLogger().warning("Sending ping to player");

            player.sendData("geyserlink:main", String.format("From Bungeecord: %d", upto++).getBytes());
            player.getServer().getInfo().sendData("geyserlink:main", String.format("From Bungeecord: %d", upto++).getBytes());

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

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        getLogger().warning("Message[" + event.getTag() + "]: " + new String(event.getData()));
    }
}
