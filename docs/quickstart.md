## Maven Dependency
Add the following to your `pom.xml`:

```xml
<repositories>
    <!-- Bundabrg's Repo -->
    <repository>
        <id>bundabrg-repo</id>
        <url>https://repo.worldguard.com.au/repository/maven-public</url>
        <releases>
            <enabled>true</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>au.com.grieve.geyserlink</groupId>
        <artifactId>GeyserLink</artifactId>
        <version>1.1.0-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

## Download the Plugin
Download and place the GeyserLink plugin into the plugins folder of each of your servers that need it.

When the server is started it will generate a `config.yml` and a `dynamic.yml` file that will contain keys
for this server.

!!! note
    [GeyserMC](https://geysermc.org), a bedrock to java proxy, does not yet support plugins so you will need
    to either wait till [this pull request](https://github.com/GeyserMC/Geyser/pull/742) is merged or build your
    own version from that branch.
    
## Sending a PingMessage

GeyserLink supports some build in messages that it will respond to itself. One of them is a PingMessage which
simply responds with whatever data was in the PingMessage payload.  This is a useful way of finding out what
other GeyserLink services are available.

!!! example
    ```java
    GeyserLink.getInstance().sendMessage(player, new PingMessage("Hello world!"));
    ```
    
This is not terribly useful as we want to capture the response. The `sendMessage` method allows you to
chain a `onResponse` call that allows you to define a lambda to run when a response to the messages is
received. Note that you can get multiple responses for some messages.

Lets capture the response and print out the packet. Note that we specify what the response class is as well
which in this case is a `PingResponse`.

!!! example
    ```java
    GeyserLink.getInstance().sendMessage(player, new PingMessage("Hello world!"))
        .onResponse(PingResponse.class, (result, signed, response) -> {
            // We have recieved a response to our ping. Print it out
            getLogger().info("Got a PingResponse: " + response);
        });
    ```

