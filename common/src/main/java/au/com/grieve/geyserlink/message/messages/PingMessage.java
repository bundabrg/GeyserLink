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

import au.com.grieve.geyserlink.message.wrappers.GeyserLinkSignedMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.ToString;

import java.io.IOException;
import java.util.Base64;


@Getter
@ToString
public class PingMessage extends WrappedMessage {
    private final String channel = "geyserlink:main";
    private final String subChannel = "ping";

    private final String data;

    public PingMessage(String data) {
        super();
        this.data = data;
    }

    public PingMessage(JsonNode node) {
        super(node);
        this.data = node.get("data").asText();
    }

    public PingMessage(GeyserLinkSignedMessage<GeyserLinkMessage> message) throws IOException {
        this(from(Base64.getDecoder().decode(message.getMessage().getPayload())));
    }

    @Override
    protected ObjectNode serialize() {
        return super.serialize()
                .put("data", data);
    }
}
