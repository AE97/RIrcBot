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
package net.ae97.rircbot;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.ae97.rircbot.configuration.ConfigurationSection;
import net.ae97.rircbot.configuration.InvalidConfigurationException;
import net.ae97.rircbot.configuration.file.YamlConfiguration;
import net.ae97.rircbot.plugin.PluginManager;
import net.ae97.rircbot.server.Server;

/**
 * @author Lord_Ralex
 */
public final class RIrcBot {

    private final static Logger logger = Logger.getLogger("Rircbot");

    public static RIrcBot getInstance() {
        return Main.getBot();
    }

    public static Logger getLogger() {
        return logger;
    }

    private final YamlConfiguration configuration;
    private final PluginManager pluginManager;
    private final List<Server> servers = new LinkedList<>();

    protected RIrcBot() {
        configuration = new YamlConfiguration();
        pluginManager = new PluginManager();
    }

    protected void start() throws IOException, InvalidConfigurationException {
        configuration.load(new File("config.yml"));
        ConfigurationSection serverConfig = configuration.getConfigurationSection("servers");
        for (String server : serverConfig.getKeys(false)) {
            Server newServer = new Server(this);
            try {
                newServer.connect(serverConfig.getString("ip"), serverConfig.getInt("port", 6667), serverConfig.getString("bind", null));
            } catch (IOException ex) {
                getLogger().log(Level.SEVERE, "Error on connecting to " + server, ex);
            }
            servers.add(newServer);
        }
    }

    public void shutdown() {
        for (Server server : servers) {
            server.disconnect();
        }
        servers.clear();
        try {
            pluginManager.join();
        } catch (InterruptedException ex) {
        }
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public YamlConfiguration getConfiguration() {
        return configuration;
    }

    public List<Server> getServers() {
        return servers;
    }
}
