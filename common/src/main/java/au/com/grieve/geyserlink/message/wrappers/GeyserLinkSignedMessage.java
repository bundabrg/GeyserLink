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

package au.com.grieve.geyserlink.message.wrappers;

import au.com.grieve.geyserlink.GeyserLink;
import au.com.grieve.geyserlink.message.BaseMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.ToString;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;

@SuppressWarnings("unused")
@Getter
@ToString(callSuper = true)
public class GeyserLinkSignedMessage<T extends EnvelopeMessage> extends BaseMessage {
    private final String payload;
    private final String signature;
    private final Class<T> messageClass;

    private final T message;

    public GeyserLinkSignedMessage(String payload, String signature, Class<T> messageClass) {
        this.payload = payload;
        this.signature = signature;
        this.messageClass = messageClass;

        // Extract message from payload
        T message = null;
        try {
            message = messageClass.getConstructor(JsonNode.class).newInstance(from(payload));
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException | IOException e) {
            e.printStackTrace();
        }
        this.message = message;
    }

    public GeyserLinkSignedMessage(JsonNode node, Class<T> messageClass) {
        super(node);
        this.payload = node.get("payload").asText();
        this.signature = node.get("signature").asText();
        this.messageClass = messageClass;

        // Extract message from payload
        T message = null;
        try {
            message = messageClass.getConstructor(JsonNode.class).newInstance(from(payload));
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException | IOException e) {
            e.printStackTrace();
        }
        this.message = message;
    }

    public static <T extends EnvelopeMessage> GeyserLinkSignedMessage<T> createSignedMessage(T message, PrivateKey key, Class<T> messageClass) {
        String signature = null;
        String payload = message.serialize().toString();

        try {
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initSign(key);
            sign.update(payload.getBytes());
            signature = Base64.getEncoder().encodeToString(sign.sign());
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
        return new GeyserLinkSignedMessage<>(payload, signature, messageClass);
    }

    /**
     * Return true if signature is valid for the specific public key
     */
    public boolean isValid(PublicKey key) {
        try {
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initVerify(key);
            sign.update(payload.getBytes());
            return sign.verify(Base64.getDecoder().decode(signature));
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException ignored) {
        }
        return false;
    }

    /**
     * Returns true if the signature is valid
     */
    @ToString.Include
    public boolean isValid() {
        PublicKey key = GeyserLink.getInstance().getKnownKeys().get(message.getSender());
        if (key != null) {
            return isValid(key);
        }
        return false;
    }

    @ToString.Include
    public boolean isKnown() {
        return GeyserLink.getInstance().getKnownKeys().containsKey(message.getSender());
    }

    @ToString.Include
    public boolean isTrusted() {
        return GeyserLink.getInstance().getTrustedKeys().containsKey(message.getSender());
    }


    @Override
    protected ObjectNode serialize() {
        return super.serialize()
                .put("payload", payload)
                .put("signature", signature);
    }
}
