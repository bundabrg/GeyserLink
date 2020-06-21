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

package au.com.grieve.geyserlink.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
@RequiredArgsConstructor
public class GeyserLinkMessage extends BaseMessage {
    public static final GeyserLinkMessage EMPTY = new GeyserLinkMessage(-1, UUID.randomUUID(), "", "", new byte[]{});

    private final int id;
    private final UUID sender;
    private final String channel;
    private final String subChannel;
    private final byte[] payload;

    public static GeyserLinkMessage fromBytes(byte[] buffer) {
        GeyserLinkMessage message;

        try {
            return (GeyserLinkMessage) new ObjectInputStream(new ByteArrayInputStream(buffer)).readObject();
        } catch (IOException | ClassNotFoundException e) {
            return EMPTY;
        }
    }
}
