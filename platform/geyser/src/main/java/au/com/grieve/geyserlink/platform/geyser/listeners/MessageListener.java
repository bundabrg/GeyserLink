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

import au.com.grieve.geyserlink.models.GeyserLinkMessage;
import au.com.grieve.geyserlink.models.GeyserLinkResponse;
import au.com.grieve.geyserlink.platform.geyser.GeyserLink;
import au.com.grieve.geyserlink.platform.geyser.events.GeyserLinkMessageEvent;
import au.com.grieve.geyserlink.platform.geyser.events.GeyserLinkResponseEvent;
import org.geysermc.connector.event.annotations.Event;
import org.geysermc.connector.event.events.PluginMessageEvent;


public class MessageListener {
    private final GeyserLink plugin;

    public MessageListener(GeyserLink plugin) {
        this.plugin = plugin;
        plugin.getConnector().registerPluginChannel("geyserlink:message");
        plugin.getConnector().registerPluginChannel("geyserlink:response");
    }

    @Event
    public void onPluginMessage(PluginMessageEvent event) {
        switch (event.getChannel()) {
            case "geyserlink:message":
                plugin.getEventManager().triggerEvent(
                        new GeyserLinkMessageEvent(event.getSession(), GeyserLinkMessage.fromBytes(event.getData())));

                break;
            case "geyserlink:response":
                plugin.getEventManager().triggerEvent(
                        new GeyserLinkResponseEvent(event.getSession(), GeyserLinkResponse.fromBytes(event.getData())));
                break;
        }
    }

    /**
     * GeyserLink provides a "geyserlink:main" channel
     */
    @Event
    public void onGeyserLinkMessage(GeyserLinkMessageEvent event) {
        if (!event.getMessage().getChannel().equals("geyserlink:main")) {
            return;
        }

        switch (event.getMessage().getSubChannel().toLowerCase()) {
            case "ping": // Respond back with the payload
                plugin.sendResponse(
                        event.getSession(),
                        event.getMessage(),
                        event.getMessage().getPayload()
                );
                break;
        }
    }

    /**
     * Handler responses we are watching for
     */
    @Event
    public void onGeyserLinkResponse(GeyserLinkResponseEvent event) {
        GeyserLink.MessageResult result = plugin.getResponseMap().get(event.getResponse().getId());
        if (result != null) {
            result.getRunnable().run(result, event.getResponse());
        }
    }

}
