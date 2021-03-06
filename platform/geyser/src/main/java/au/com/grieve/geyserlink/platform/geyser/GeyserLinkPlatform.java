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

import au.com.grieve.geyserlink.GeyserLink;
import au.com.grieve.geyserlink.IGeyserLinkPlatform;
import au.com.grieve.geyserlink.IScheduledTask;
import au.com.grieve.geyserlink.message.wrappers.GeyserLinkSignedMessage;
import lombok.Getter;
import org.geysermc.connector.network.session.GeyserSession;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Getter
public class GeyserLinkPlatform implements IGeyserLinkPlatform {
    private final GeyserLinkPlugin plugin;
    private final GeyserLink geyserLink;
    private final GeyserLinkLogger logger;

    public GeyserLinkPlatform(GeyserLinkPlugin plugin) {
        this.plugin = plugin;
        this.logger = new GeyserLinkLogger(plugin.getLogger());
        this.geyserLink = new GeyserLink(this);
    }

    @Override
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    @Override
    public InputStream getPlatformResourceAsStream(String name) {
        return plugin.getResourceAsStream(String.format("platform/geyser/%s", name));
    }

    @Override
    public void sendPluginMessage(Object recipient, String channel, GeyserLinkSignedMessage<?> message) {
        if (recipient instanceof GeyserSession) {
            ((GeyserSession) recipient).sendPluginMessage(channel, message.getBytes());
        }
    }

    @Override
    public IScheduledTask schedule(Runnable runnable, long delay) {
        return new ScheduledTask(plugin.getConnector().getGeneralThreadPool().schedule(runnable, delay, TimeUnit.SECONDS));
    }
}
