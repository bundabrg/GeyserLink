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

package au.com.grieve.geyserlink.platform.spigot;

import au.com.grieve.geyserlink.platform.spigot.listeners.MessageListener;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;


@Getter
public final class GeyserLinkPlugin extends JavaPlugin {
    private final GeyserLinkPlatform platform;

    public GeyserLinkPlugin() {
        super();
        platform = new GeyserLinkPlatform(this);
    }

    @Override
    public void onEnable() {
        // Register Listeners
        getServer().getPluginManager().registerEvents(new MessageListener(this), this);
    }
}
