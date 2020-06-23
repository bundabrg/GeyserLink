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

package au.com.grieve.geyserlink.message.responses;

import au.com.grieve.geyserlink.message.wrappers.EnvelopeMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.ToString;

import java.util.Base64;
import java.util.UUID;

@Getter
@ToString(callSuper = true)
public class GeyserLinkResponse extends EnvelopeMessage {
    private final UUID recipient;
    private final byte[] payload;

    public GeyserLinkResponse(int id, UUID sender, UUID recipient, byte[] payload) {
        super(id, sender);
        this.recipient = recipient;
        this.payload = payload;
    }

    public GeyserLinkResponse(JsonNode node) {
        super(node);
        this.recipient = UUID.fromString(node.get("recipient").asText());
        this.payload = Base64.getDecoder().decode(node.get("payload").asText());
    }

    @Override
    protected ObjectNode serialize() {
        return super.serialize()
                .put("recipient", recipient.toString())
                .put("payload", Base64.getEncoder().encodeToString(payload));
    }
}
