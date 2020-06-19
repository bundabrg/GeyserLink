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

import com.google.common.collect.Iterables;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.concurrent.TimeUnit;

public class GeyserLink extends Plugin implements Listener {
    private int upto = 0;

    @Override
    public void onEnable() {
        super.onEnable();

        // Register Channels
        getProxy().registerChannel("geyerlink:main");
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getScheduler().schedule(this, () -> {

            ProxiedPlayer player = Iterables.getFirst(getProxy().getPlayers(), null);
            if (player == null) {
                return;
            }

            getLogger().warning("Sending ping to player");

            player.sendData("geyserlink:main", String.format("From Bungeecord: %d", upto++).getBytes());
            player.getServer().getInfo().sendData("geyserlink:main", String.format("From Bungeecord: %d", upto++).getBytes());

        }, 5, 5, TimeUnit.SECONDS);
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        getLogger().warning("Message[" + event.getTag() + "]: " + new String(event.getData()));
    }
}
