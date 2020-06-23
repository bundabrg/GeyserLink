[![MIT license](https://img.shields.io/badge/License-MIT-blue.svg)](https://lbesson.mit-license.org/)
[![GitHub release](https://img.shields.io/github/release/Bundabrg/GeyserLink)](https://GitHub.com/Bundabrg/GeyserLink/releases/)
[![GitHub commits](https://img.shields.io/github/commits-since/Bundabrg/GeyserLink/latest)](https://GitHub.com/Bundabrg/GeyserLink/commit/)
[![Github all releases](https://img.shields.io/github/downloads/Bundabrg/GeyserLink/total.svg)](https://GitHub.com/Bundabrg/GeyserLink/releases/)
![HitCount](http://hits.dwyl.com/bundabrg/GeyserLink.svg)

![Workflow](https://github.com/bundabrg/GeyserLink/workflows/build/badge.svg)
[![Maintenance](https://img.shields.io/badge/Maintained%3F-yes-green.svg)](https://GitHub.com/Bundabrg/GeyserLink/graphs/commit-activity)
[![GitHub contributors](https://img.shields.io/github/contributors/Bundabrg/GeyserLink)](https://GitHub.com/Bundabrg/GeyserLink/graphs/contributors/)
[![GitHub issues](https://img.shields.io/github/issues/Bundabrg/GeyserLink)](https://GitHub.com/Bundabrg/GeyserLink/issues/)
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/Bundabrg/GeyserLink.svg)](http://isitmaintained.com/project/Bundabrg/GeyserLink "Average time to resolve an issue")
[![GitHub pull-requests](https://img.shields.io/github/issues-pr/Bundabrg/GeyserLink)](https://GitHub.com/Bundabrg/GeyserLink/pull/)
 

---

[**Documentation**](https://bundabrg.github.io/GeyserLink/)

[**Source Code**](https://github.com/bundabrg/GeyserLink/)

---

*GeyserLink is only of use to developers or plugins that rely on GeyserLink to be available. It does not itself provide any additional feature.*

## What is GeyserLink

GeyserLink aims to provide an easy method of sending messages between any server involved in a Minecraft connection and to do
so in such a way as to allow both a trusted setup and an untrusted setup. This could also potentially be a useful way for client side
mods to implement better communication. It is the TCP/IP of PluginMessages.

An example configuration could be a Geyser server connected to a Bungeecord server connecting to a Spigot server. If all three servers are
under the control of the same user then they can be configured to trust each other. However if the Geyser server is instead run by
another user (for example someone connecting to a server using their own Proxy) then it will be untrusted but still be able to
participate in communication where no trust is needed.

One example is that a trusted proxy could be queried about a players real IP whereas an untrusted one cannot be trusted to provide this and thus
a plugin relying on this behaviour can gracefully fallback to using the proxy IP.

The following configurations should be supported:

* Owner who has multiple proxies connecting to them for load balancing reasons. These are trusted.
* Owner who doesn't run their own proxy but still wants to provide support for users to run their own. These are untrusted.
* Mix of the above.
* A Client Side mod connected to any of the above. In this case the client side mod would be untrusted but the servers could
be trusted.

Presently GeyserLink can be used as a plugin for the following servers:

* GeyserMC
* Spigot
* Bungeecord

## Features

* Provide a secure messaging system utilizing the built-in minecraft plugin messages.  All messages are signed with a private key and all messages
can be verified by other participants as being valid.
* Automatically discovers participant keys and will record them.
* Easily convert an untrusted member into trusted by copying its public key in `dynamic.yml` config file.
* Messages are linked to their responses using a unique sequence ID.
* Provides a lambda style callback function so that message responses can be provided close in code to where messages are generated.
* Supports multiple responses as some messages may require more than one participant to respond
* Easily create custom messages


## Examples

Send a ping message out and write out to the log any responses received.
```java
// Will get a response from every participant
GeyserLink.getInstance().sendMessage(player, new PingMessage("Hello world!"))
    .onResponse(PingResponse.class, (result, signed, response) -> {
        getLogger().info("Got a ping response: " + response);
    });
```

Send a custom message and retrieve a custom complex response
```java
// Will get a response from every participant
GeyserLink.getInstance().sendMessage(player, new GetPlayerProfileMessage("bundie"))
    .onResponse(PingResponse.class, (result, signed, response) -> {
        // Only accept trusted responses
        if (signed.isTrusted()) {
            getLogger().info(String.format("name:%s, location:%s world:%s",
                    response.getName(), response.getLocation().toString(), response.getWorld()));
        }
    });
```
