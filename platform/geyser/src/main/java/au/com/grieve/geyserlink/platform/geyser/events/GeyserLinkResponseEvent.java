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
import au.com.grieve.geyserlink.messages.GeyserLinkResponse;
import au.com.grieve.geyserlink.messages.GeyserLinkSignedMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.geysermc.connector.event.events.CancellableGeyserEvent;
import org.geysermc.connector.network.session.GeyserSession;

@Getter
@ToString
@AllArgsConstructor
public class GeyserLinkResponseEvent extends CancellableGeyserEvent {
    private final GeyserLink geyserLink;
    private final GeyserSession session;
    private final GeyserLinkSignedMessage<GeyserLinkResponse> signedMessage;
}
