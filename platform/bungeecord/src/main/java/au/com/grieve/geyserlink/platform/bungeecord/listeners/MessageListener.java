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

package au.com.grieve.geyserlink.platform.bungeecord.listeners;

import au.com.grieve.geyserlink.models.GeyserLinkMessage;
import au.com.grieve.geyserlink.models.GeyserLinkResponse;
import au.com.grieve.geyserlink.platform.bungeecord.GeyserLink;
import au.com.grieve.geyserlink.platform.bungeecord.events.GeyserLinkMessageEvent;
import au.com.grieve.geyserlink.platform.bungeecord.events.GeyserLinkResponseEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class MessageListener implements Listener {
    private final GeyserLink plugin;

    public MessageListener(GeyserLink plugin) {
        this.plugin = plugin;

        // Register Channels
        plugin.getProxy().registerChannel("geyerlink:message");
        plugin.getProxy().registerChannel("geyerlink:response");
    }

    @EventHandler
    public void onPluginMessageReceived(PluginMessageEvent event) {
        switch (event.getTag()) {
            case "geyserlink:message":
                plugin.getProxy().getPluginManager().callEvent(
                        new GeyserLinkMessageEvent(event.getSender(), GeyserLinkMessage.fromBytes(event.getData()))
                );
                break;
            case "geyserlink:response":
                plugin.getProxy().getPluginManager().callEvent(
                        new GeyserLinkResponseEvent(event.getSender(), GeyserLinkResponse.fromBytes(event.getData())));
                break;
        }
    }

    /**
     * GeyserLink provides a "geyserlink:main" channel
     */
    @EventHandler
    public void onGeyserLinkMessage(GeyserLinkMessageEvent event) {
        if (!event.getMessage().getChannel().equals("geyserlink:main")) {
            return;
        }

        switch (event.getMessage().getSubChannel().toLowerCase()) {
            case "ping":
                plugin.sendResponse(
                        event.getConnection(),
                        event.getMessage(),
                        event.getMessage().getPayload()
                );
                break;
        }
    }

    /**
     * Handler responses we are watching for
     */
    @EventHandler
    public void onGeyserLinkResponse(GeyserLinkResponseEvent event) {
        if (!event.getResponse().getTarget().equals(GeyserLink.SOURCE)) {
            return;
        }

        if (plugin.getResponseMap().containsKey(event.getResponse().getId())) {
            plugin.getResponseMap().get(event.getResponse().getId()).run(event.getResponse());
        }
    }
}
