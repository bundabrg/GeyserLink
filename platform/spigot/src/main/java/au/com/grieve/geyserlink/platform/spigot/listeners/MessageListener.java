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

package au.com.grieve.geyserlink.platform.spigot.listeners;

import au.com.grieve.geyserlink.message.messages.GeyserLinkMessage;
import au.com.grieve.geyserlink.message.responses.GeyserLinkResponse;
import au.com.grieve.geyserlink.message.wrappers.GeyserLinkSignedMessage;
import au.com.grieve.geyserlink.platform.spigot.GeyserLinkPlugin;
import au.com.grieve.geyserlink.platform.spigot.events.GeyserLinkMessageEvent;
import au.com.grieve.geyserlink.platform.spigot.events.GeyserLinkResponseEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@SuppressWarnings("unused")
public class MessageListener implements Listener, PluginMessageListener {
    private final GeyserLinkPlugin plugin;

    public MessageListener(GeyserLinkPlugin plugin) {
        this.plugin = plugin;

        // Register Channels
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "geyserlink:message");
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "geyserlink:response");
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "geyserlink:message", this);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "geyserlink:response", this);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] bytes) {
        try {
            switch (channel) {
                case "geyserlink:message":
                    plugin.getServer().getPluginManager().callEvent(
                            new GeyserLinkMessageEvent(plugin.getPlatform().getGeyserLink(), player,
                                    new GeyserLinkSignedMessage<>(GeyserLinkSignedMessage.from(bytes), GeyserLinkMessage.class)));
                    break;
                case "geyserlink:response":
                    plugin.getServer().getPluginManager().callEvent(
                            new GeyserLinkResponseEvent(plugin.getPlatform().getGeyserLink(), player,
                                    new GeyserLinkSignedMessage<>(GeyserLinkSignedMessage.from(bytes), GeyserLinkResponse.class)));
                    break;
            }
        } catch (IOException ignored) {
        }
    }

    /**
     * GeyserLink provides a "geyserlink:main" channel
     */
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @EventHandler
    public void onGeyserLinkMessage(GeyserLinkMessageEvent event) {
        if (!event.getSignedMessage().getMessage().getChannel().equals("geyserlink:main")) {
            return;
        }

        switch (event.getSignedMessage().getMessage().getSubChannel().toLowerCase()) {
            default: // Common Stuff
                plugin.getPlatform().getGeyserLink().handleMainMessage(event.getPlayer(), event.getSignedMessage());
        }
    }

    /**
     * Handler responses we are watching for
     */
    @EventHandler
    public void onGeyserLinkResponse(GeyserLinkResponseEvent event) {
        event.getGeyserLink().handleResponse(event.getPlayer(), event.getSignedMessage());
    }
}
