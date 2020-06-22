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

package au.com.grieve.geyserlink.platform.bungeecord;

import au.com.grieve.geyserlink.GeyserLink;
import au.com.grieve.geyserlink.platform.bungeecord.listeners.MessageListener;
import com.google.common.collect.Iterables;
import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.concurrent.TimeUnit;

@Getter
public class GeyserLinkPlugin extends Plugin {
    private GeyserLinkPlatform platform;

    private int upto = 0;

    @Override
    public void onEnable() {
        super.onEnable();

        platform = new GeyserLinkPlatform(this);

        // Register Listeners
        getProxy().getPluginManager().registerListener(this, new MessageListener(this));

        // Test
        getProxy().getScheduler().schedule(this, () -> {

            ProxiedPlayer player = Iterables.getFirst(getProxy().getPlayers(), null);
            if (player == null) {
                return;
            }

            getLogger().warning("Sending ping to player");

            GeyserLink.getInstance().sendMessage(
                    player,
                    "geyserlink:main",
                    "ping",
                    new byte[]{(byte) upto++}
            ).onResponse((result, response) -> getLogger().warning("Got a ping response: " + response));

        }, 5, 5, TimeUnit.SECONDS);
    }

}
