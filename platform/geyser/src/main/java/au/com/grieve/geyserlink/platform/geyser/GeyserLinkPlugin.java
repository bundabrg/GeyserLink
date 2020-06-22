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

import au.com.grieve.geyserlink.platform.geyser.listeners.MessageListener;
import com.google.common.collect.Iterables;
import lombok.Getter;
import org.geysermc.connector.event.annotations.Event;
import org.geysermc.connector.event.events.PluginEnableEvent;
import org.geysermc.connector.network.session.GeyserSession;
import org.geysermc.connector.plugin.GeyserPlugin;
import org.geysermc.connector.plugin.PluginClassLoader;
import org.geysermc.connector.plugin.PluginManager;
import org.geysermc.connector.plugin.annotations.Plugin;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
@Plugin(
        name = "GeyserLink",
        version = "1.1.0-dev",
        authors = {"Bundabrg"},
        description = "The Missing Link"
)
@Getter
public class GeyserLinkPlugin extends GeyserPlugin {
    private final GeyserLinkPlatform platform;

    private int upto = 0;

    public GeyserLinkPlugin(PluginManager pluginManager, PluginClassLoader pluginClassLoader) {
        super(pluginManager, pluginClassLoader);

        platform = new GeyserLinkPlatform(this);
    }

    @Event
    public void onEnable(PluginEnableEvent event) {
        if (event.getPlugin() == this) {
            // Register Listeners
            registerEvents(new MessageListener(this));


            // Test
            getConnector().getGeneralThreadPool().scheduleAtFixedRate(() -> {
                GeyserSession session = Iterables.getFirst(getConnector().getPlayers().values(), null);

                if (session == null) {
                    return;
                }

                getLogger().warning("Sending ping to player");
                platform.getGeyserLink().sendMessage(
                        session,
                        "geyserlink:main",
                        "ping",
                        new byte[]{(byte) upto++}
                ).onResponse((result, response) -> getLogger().warning("Got a ping response: " + response));

            }, 5, 5, TimeUnit.SECONDS);
        }
    }
}
