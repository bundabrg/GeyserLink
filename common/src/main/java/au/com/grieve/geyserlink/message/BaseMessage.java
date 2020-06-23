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

package au.com.grieve.geyserlink.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.IOException;

@ToString
@RequiredArgsConstructor
public abstract class BaseMessage {

    public BaseMessage(JsonNode node) {
    }

    protected ObjectNode serialize() {
        return new ObjectMapper(new YAMLFactory()).createObjectNode();
    }

    public byte[] getBytes() {
        return serialize().toString().getBytes();
    }

    public static JsonNode from(String payload) throws IOException {
        return new ObjectMapper(new YAMLFactory()).readTree(payload);
    }

    public static JsonNode from(byte[] payload) throws IOException {
        return from(new String(payload));
    }

}
