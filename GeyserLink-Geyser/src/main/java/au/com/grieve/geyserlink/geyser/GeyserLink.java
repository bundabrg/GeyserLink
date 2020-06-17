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

package au.com.grieve.geyserlink.geyser;

import com.google.common.collect.Iterables;
import org.geysermc.connector.GeyserConnector;
import org.geysermc.connector.event.EventContext;
import org.geysermc.connector.event.annotations.Event;
import org.geysermc.connector.event.events.PluginEnableEvent;
import org.geysermc.connector.event.events.PluginMessageEvent;
import org.geysermc.connector.network.session.GeyserSession;
import org.geysermc.connector.plugin.GeyserPlugin;
import org.geysermc.connector.plugin.PluginClassLoader;
import org.geysermc.connector.plugin.PluginManager;
import org.geysermc.connector.plugin.annotations.Plugin;

import java.util.concurrent.TimeUnit;

@Plugin(
        name = "EduSupport",
        version = "1.1.0-dev",
        authors = {"Bundabrg"},
        description = "Provides protocol support for Minecraft Educational Edition"
)
public class GeyserLink extends GeyserPlugin {
    private int upto = 0;

    public GeyserLink(PluginManager pluginManager, PluginClassLoader pluginClassLoader) {
        super(pluginManager, pluginClassLoader);
    }

    @Event
    public void onEnable(EventContext ctx, PluginEnableEvent event) {
        if (event.getPlugin() == this) {
            GeyserConnector.getInstance().registerPluginChannel("geyserlink:main");
            GeyserConnector.getInstance().getGeneralThreadPool().scheduleAtFixedRate(() -> {
                GeyserSession player = Iterables.getFirst(GeyserConnector.getInstance().getPlayers().values(), null);
                if (player == null) {
                    return;
                }

                GeyserConnector.getInstance().getLogger().warning("Sending ping to player");

                player.sendPluginMessage("geyserlink:main", String.format("From Geyser: %d", upto++).getBytes());

            }, 5, 5, TimeUnit.SECONDS);
        }
    }

    @Event
    public void onPluginMessage(EventContext ctx, PluginMessageEvent event) {
        GeyserConnector.getInstance().getLogger().warning("Message[" + event.getChannel() + "]: " + new String(event.getData()));
    }

}
