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
package net.ae97.rircbot.event.user;

import net.ae97.rircbot.event.Event;
import net.ae97.rircbot.recipient.User;
import net.ae97.rircbot.snapshot.UserSnapshot;

/**
 * @author Lord_Ralex
 */
public abstract class UserEvent implements Event {

    protected final User user;
    protected final UserSnapshot userSnapshot;

    protected UserEvent(User u) {
        user = u;
        userSnapshot = user.generateSnapshot();
    }

    public User getUser() {
        return user;
    }

    public UserSnapshot getUserSnapshot() {
        return userSnapshot;
    }
}
