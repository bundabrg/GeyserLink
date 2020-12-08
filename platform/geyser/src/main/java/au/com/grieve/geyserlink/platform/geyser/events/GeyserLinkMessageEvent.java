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

package au.com.grieve.geyserlink.platform.geyser.events;

import au.com.grieve.geyserlink.GeyserLink;
import au.com.grieve.geyserlink.message.messages.GeyserLinkMessage;
import au.com.grieve.geyserlink.message.wrappers.GeyserLinkSignedMessage;
import lombok.*;
import org.geysermc.connector.event.Cancellable;
import org.geysermc.connector.event.EventSession;
import org.geysermc.connector.event.GeyserEvent;
import org.geysermc.connector.network.session.GeyserSession;

@Data
@EqualsAndHashCode(callSuper = true)
public class GeyserLinkMessageEvent extends GeyserEvent implements Cancellable, EventSession {
    private boolean cancelled;
    private final GeyserLink geyserLink;
    private final GeyserSession session;
    private final GeyserLinkSignedMessage<GeyserLinkMessage> signedMessage;
}
