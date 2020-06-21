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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

@SuppressWarnings("unchecked")
@EqualsAndHashCode(callSuper = true)
@ToString
@Getter
public class GeyserLinkSignedMessage<T extends BaseMessage> extends BaseMessage {
    private final byte[] payload;
    private final byte[] signature;
    private transient T message;

    public GeyserLinkSignedMessage(byte[] payload, byte[] signature) {
        this.payload = payload;
        this.signature = signature;
    }

    public static <T extends BaseMessage> GeyserLinkSignedMessage<T> sign(T message, PrivateKey key) {
        byte[] messageBytes = message.getBytes();
        byte[] signature = null;

        try {
            Signature sign;
            sign = Signature.getInstance("SHA256withRSA");
            sign.initSign(key);
            sign.update(messageBytes);
            signature = sign.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }

        return new GeyserLinkSignedMessage<>(messageBytes, signature);
    }

    @SuppressWarnings("unchecked")
    public static <T extends BaseMessage> GeyserLinkSignedMessage<T> fromBytes(byte[] buffer) {
        try {
            return (GeyserLinkSignedMessage<T>) new ObjectInputStream(new ByteArrayInputStream(buffer)).readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

    public T getMessage() {
        if (message == null) {
            try {
                message = (T) new ObjectInputStream(new ByteArrayInputStream(payload)).readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return message;
    }
}
