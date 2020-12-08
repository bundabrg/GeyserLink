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

package au.com.grieve.geyserlink.platform.geyser.listeners;

import au.com.grieve.geyserlink.message.messages.GeyserLinkMessage;
import au.com.grieve.geyserlink.message.responses.GeyserLinkResponse;
import au.com.grieve.geyserlink.message.wrappers.GeyserLinkSignedMessage;
import au.com.grieve.geyserlink.platform.geyser.GeyserLinkPlugin;
import au.com.grieve.geyserlink.platform.geyser.events.GeyserLinkMessageEvent;
import au.com.grieve.geyserlink.platform.geyser.events.GeyserLinkResponseEvent;
import org.geysermc.connector.event.annotations.GeyserEventHandler;
import org.geysermc.connector.event.events.packet.downstream.ServerPluginMessagePacketReceive;

import java.io.IOException;


@SuppressWarnings("unused")
public class MessageListener {
    private final GeyserLinkPlugin plugin;

    public MessageListener(GeyserLinkPlugin plugin) {
        this.plugin = plugin;
        plugin.getConnector().registerPluginChannel("geyserlink:message");
        plugin.getConnector().registerPluginChannel("geyserlink:response");
    }

    @GeyserEventHandler
    public void onPluginMessage(ServerPluginMessagePacketReceive event) {
        try {
            switch (event.getPacket().getChannel()) {
                case "geyserlink:message":
                    plugin.getEventManager().triggerEvent(
                            new GeyserLinkMessageEvent(plugin.getPlatform().getGeyserLink(), event.getSession(),
                                    new GeyserLinkSignedMessage<>(GeyserLinkSignedMessage.from(event.getPacket().getData()), GeyserLinkMessage.class)));
                    break;
                case "geyserlink:response":
                    plugin.getEventManager().triggerEvent(
                            new GeyserLinkResponseEvent(plugin.getPlatform().getGeyserLink(), event.getSession(),
                                    new GeyserLinkSignedMessage<>(GeyserLinkSignedMessage.from(event.getPacket().getData()), GeyserLinkResponse.class)));
                    break;
            }
        } catch (IOException ignored) {
        }
    }

    /**
     * GeyserLink provides a "geyserlink:main" channel
     */
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @GeyserEventHandler
    public void onGeyserLinkMessage(GeyserLinkMessageEvent event) {
        if (!event.getSignedMessage().getMessage().getChannel().equals("geyserlink:main")) {
            return;
        }

        switch (event.getSignedMessage().getMessage().getSubChannel().toLowerCase()) {
            default: // Common Stuff
                plugin.getPlatform().getGeyserLink().handleMainMessage(event.getSession(), event.getSignedMessage());
        }
    }

    /**
     * Handler responses we are watching for
     */
    @GeyserEventHandler
    public void onGeyserLinkResponse(GeyserLinkResponseEvent event) {
        event.getGeyserLink().handleResponse(event.getSession(), event.getSignedMessage());
    }

}
