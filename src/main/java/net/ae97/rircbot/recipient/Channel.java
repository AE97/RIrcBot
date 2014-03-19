/*
 * Copyright (C) 2014 Lord_Ralex
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
package net.ae97.rircbot.recipient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.ae97.rircbot.cache.Cacheable;
import net.ae97.rircbot.server.Server;
import net.ae97.rircbot.snapshot.ChannelSnapshot;
import net.ae97.rircbot.snapshot.Snapshotable;

/**
 * @author Lord_Ralex
 */
public class Channel implements MessageRecipient, NoticeRecipient, FlagRecipient, Snapshotable<ChannelSnapshot>, Cacheable {

    private static final Map<String, Channel> channelCache = new ConcurrentHashMap<>();

    private Channel() {
    }

    @Override
    public void sendMessage(String... message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sendNotice(String... message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<String> getFlags(Channel chan) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setFlags(List<String> flags) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setFlag(String flag, boolean setting) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setFlags(Map<String, Boolean> flags) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ChannelSnapshot generateSnapshot() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getName() {
        return null;
    }

    public static Channel getChannel(Server server, String name) {
        Channel chan = channelCache.get(name.toLowerCase());
        if (chan == null) {
            chan = new Channel();
            channelCache.put(name.toLowerCase(), chan);
        }
        return chan;
    }
}
