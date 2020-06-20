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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

@Data
@ToString
@AllArgsConstructor
public class GeyserLinkResponse implements Serializable {
    private final int id;
    private final String source;
    private final String target;
    private final byte[] payload;
    private final String signature;

    public static GeyserLinkResponse fromBytes(byte[] buffer) {
        GeyserLinkResponse message;

        try {
            return (GeyserLinkResponse) new ObjectInputStream(new ByteArrayInputStream(buffer)).readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new GeyserLinkResponse(-1, "unknown", "unknown", new byte[]{}, "");
        }
    }

    public byte[] getBytes() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            new ObjectOutputStream(bos).writeObject(this);
            return bos.toByteArray();
        } catch (IOException e) {
            return new byte[]{};
        }
    }

}
