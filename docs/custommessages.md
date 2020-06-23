Whilst we can send raw messages using GeyserLink it is easier to define a Message and Response class that will serialize and
unserialize the responses.

## Custom Message and Response

Let's create a `PlayerQueryMessage` and `PlayerQueryResponse` that returns the number of players on the server. It is up 
to you as to which server will respond to this but for this example we will assume the GeyserLink plugin is on a Spigot server 
and on a Geyser proxy and that either side may send the message to get a response from the other side. 


### PlayerQueryMessage

!!! example
    ```java
    @Getter
    @ToString
    public class PlayerQueryMessage extends WrappedMessage {
        private final String channel = "myPlugin:command";
        private final String subChannel = "player-query";
        
        public PlayerQueryMessage(String data) {
            super();
        }
    
        public PlayerQueryMessage(JsonNode node) {
            super(node);
        }
    
        @Override
        protected ObjectNode serialize() {
            return super.serialize();
        }
    }
    ```
    
Here we define the channel and subchannel that this message will use. The rest is mainly boilerplate dealing with
serializing or deserializing the object.

!!! info
     Messages make use of JSON to contain their structure in a packet. This means that when serializing an object
     it will call the `serialize` method that will add to an ObjectNode any data relevant for the object.
     
     To deserialize a constructor taking a `JsonNode` is used which then pulls from that any relevant fields for
     the object.

### PlayerQueryResponse

!!! example
    ```java
    @Getter
    @ToString
    public class PlayerQueryResponse extends WrappedResponse {
        private int count;
    
        public PlayerQueryResponse(int count) {
            super();
    
            this.count = count;
        }
    
        public PlayerQueryResponse(JsonNode node) {
            super(node);
            this.count = node.get("count").asInt();
        }
    
        @Override
        protected ObjectNode serialize() {
            return super.serialize()
                    .put("count", count);
        }
    }
    ```
    
This one is a bit more interesting as it has a data field `count`. We have to deal with how to deserialize from a JsonNode
and how to serialize to an ObjectNode.

!!! note
    The Response does not need to define a channel or subchannel.

### Putting it together

Now you can send a PlayerQueryMessage by doing something like this:

!!! example
    ```java
    GeyserLink.getInstance().sendMessage(player, new PlayerQueryMessage())
        .onResponse(PlayerQueryResponse.class, (result, signed, response) -> {
            getLogger(String.format("The server has %d players on it", response.getCount()));
        });
    ```

!!! note
    When sending a raw message you only have the fields `result` and `signed`. When using a wrapped message you also
    get a field for the message itself. In the above case `response` will be a `PlayerQueryResponse` object and will
    be deserialized from the `signed` object but we still get the `signed` object as it has data on it that could be
    useful.

## Message Event
Whichever server is responding to the message will need to register an event listener for the message. The following is a
simple example for a Spigot server.

!!! example
    ```java
    @EventHandler
    public void onGeyserLinkMessage(GeyserLinkMessageEvent event) {
        if (!event.getSignedMessage().getMessage().getChannel().equals("myPlugin:command")) {
            return;
        }
        
        switch(event.getSignedMessage().getMessage().getSubChannel()) {
            case "player-query":
                GeyserLink.getInstance().sendResponse(event.getPlayer(), event.getSignedMessage().getMessage(),
                        new PlayerQueryResponse(plugin.getServer().getOnlinePlayers().size()));
                break;
        }
    }
    ```

For completion sake the following is for the GeyserMC server, note how similar it is.

!!! example
    ```java
    @Event
    public void onGeyserLinkMessage(GeyserLinkMessageEvent event) {
        if (!event.getSignedMessage().getMessage().getChannel().equals("myPlugin:command")) {
            return;
        }
        
        switch(event.getSignedMessage().getMessage().getSubChannel()) {
            case "player-query":
                GeyserLink.getInstance().sendResponse(event.getSession(), event.getSignedMessage().getMessage(),
                        new PlayerQueryResponse(plugin.getConnector().getPlayers().values().size()));
                break;
        }
    }
    ```
    
## Security

You may have noticed an issue with the previous example.  Anyone could connect to a server and either spoof GeyserLink
messages or run their own GeyserLink plugin on a proxy and thus the player count must come from a trusted partner. We also
don't want a random person being able to query the player count.

!!! note
    If possible try support untrusted clients as well. If the player count in this example is not confidential then there may be
    no reason not to still allow queries. Also if the message is coming from a client side mod then it will be untrusted
    by default.

To solve this we need to check on both sides. On the client side the following would only accept trusted responses:

!!! example
    ```java
    GeyserLink.getInstance().sendMessage(player, new PlayerQueryMessage())
        .onResponse(PlayerQueryResponse.class, (result, signed, response) -> {
            if (signed.isTrusted()) {
                getLogger(String.format("The server has %d players on it", response.getCount()));
            }
        });
    ```
 
The server could be updated as follows:

!!! example
    ```java
    @EventHandler
    public void onGeyserLinkMessage(GeyserLinkMessageEvent event) {
        if (!event.getSignedMessage().getMessage().getChannel().equals("myPlugin:command")) {
            return;
        }
        
        switch(event.getSignedMessage().getMessage().getSubChannel()) {
            case "player-query":
                if (event.getSignedMessage().isTrusted()) {
                    GeyserLink.getInstance().sendResponse(event.getPlayer(), event.getSignedMessage().getMessage(),
                            new PlayerQueryResponse(plugin.getServer().getOnlinePlayers().size()));
                }
                break;
        }
    }
    ```

