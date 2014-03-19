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

import java.io.IOException;
import java.util.logging.Level;
import net.ae97.rircbot.configuration.InvalidConfigurationException;

/**
 * @author Lord_Ralex
 */
public class Main {

    private static final RIrcBot rircbot = new RIrcBot();

    public static void main(String[] args) {
        try {
            getBot().start();
        } catch (IOException | InvalidConfigurationException ex) {
            RIrcBot.getLogger().log(Level.SEVERE, "Error on starting RIrcBot", ex);
        }
    }

    protected static RIrcBot getBot() {
        return rircbot;
    }

    private Main() {
    }
}
