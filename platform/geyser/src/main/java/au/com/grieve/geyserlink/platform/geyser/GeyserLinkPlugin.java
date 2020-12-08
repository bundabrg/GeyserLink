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

package au.com.grieve.geyserlink.platform.geyser;

import au.com.grieve.geyserlink.platform.geyser.listeners.MessageListener;
import lombok.Getter;
import org.geysermc.connector.event.annotations.GeyserEventHandler;
import org.geysermc.connector.event.events.extension.ExtensionEnableEvent;
import org.geysermc.connector.extension.ExtensionClassLoader;
import org.geysermc.connector.extension.ExtensionManager;
import org.geysermc.connector.extension.GeyserExtension;
import org.geysermc.connector.extension.annotations.Extension;

@SuppressWarnings("unused")
@Extension(
        name = "GeyserLink",
        version = "1.1.0-dev",
        authors = {"Bundabrg"},
        description = "The Missing Link"
)
@Getter
public class GeyserLinkPlugin extends GeyserExtension {
    private final GeyserLinkPlatform platform;

    public GeyserLinkPlugin(ExtensionManager pluginManager, ExtensionClassLoader pluginClassLoader) {
        super(pluginManager, pluginClassLoader);

        platform = new GeyserLinkPlatform(this);
    }

    @GeyserEventHandler
    public void onEnable(ExtensionEnableEvent event) {
        if (event.getExtension() == this) {
            // Register Listeners
            registerEvents(new MessageListener(this));
        }
    }
}
