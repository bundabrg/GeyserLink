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

package au.com.grieve.geyserlink.message.messages;

import au.com.grieve.geyserlink.message.wrappers.EnvelopeMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.ToString;

import java.util.Base64;
import java.util.UUID;

@Getter
@ToString(callSuper = true)
public class GeyserLinkMessage extends EnvelopeMessage {
    private final String channel;
    private final String subChannel;
    private final byte[] payload;

    public GeyserLinkMessage(int id, UUID sender, String channel, String subChannel, byte[] payload) {
        super(id, sender);
        this.channel = channel;
        this.subChannel = subChannel;
        this.payload = payload;
    }

    public GeyserLinkMessage(JsonNode node) {
        super(node);
        this.channel = node.get("channel").asText();
        this.subChannel = node.get("subChannel").asText();
        this.payload = Base64.getDecoder().decode(node.get("payload").asText());
    }

    @Override
    protected ObjectNode serialize() {
        return super.serialize()
                .put("channel", channel)
                .put("subChannel", subChannel)
                .put("payload", Base64.getEncoder().encodeToString(payload));
    }
}
