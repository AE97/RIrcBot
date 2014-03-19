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
package net.ae97.rircbot.plugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.ae97.rircbot.configuration.InvalidConfigurationException;
import net.ae97.rircbot.configuration.file.YamlConfiguration;

/**
 * @author Lord_Ralex
 */
public abstract class Plugin {

    private final File dataFolder = new File("plugins", getName());
    private final Logger logger = Logger.getLogger(getName());
    private YamlConfiguration config;

    public void onLoad() {
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void getDataFolder() {
    }

    public abstract String getName();

    public final YamlConfiguration getConfig() {
        if (config == null) {
            config = new YamlConfiguration();
            try {
                config.load(new File(dataFolder, "config.yml"));
            } catch (IOException | InvalidConfigurationException e) {
                logger.log(Level.SEVERE, "Could not load config for " + getName(), e);
            }
        }
        return config;
    }
}
