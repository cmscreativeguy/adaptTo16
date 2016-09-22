/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package to.adapt.from02oak.console.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.Locale;

import static com.google.common.base.Preconditions.checkNotNull;

public enum Commands {
    EXIT("exit", new ExitCommand()),
    LS("ls", new ListCommand()),
    HELP("help", new HelpCommand()),
    CAT("cat", new CatCommand()),
    ADD("add", new AddCommand()),
    RM("rm", new RmCommand()),
    UP("up", new UpCommand()),
    COLOUR("colour", new ColourCommand());

    public static Comparator<Commands> LEXICOGRAPHICALLY_ORDER = new Comparator<Commands>() {
        @Override
        public int compare(@Nonnull Commands o1, @Nonnull Commands o2) {
            checkNotNull(o1);
            checkNotNull(o2);
            int result = o1.getName().toUpperCase().compareTo(o2.getName().toUpperCase());
            return result;
        }
    };

    private static final Logger LOG = LoggerFactory.getLogger(Commands.class);

    private final String name;
    private final Command command;

    Commands(@Nonnull String name, @Nonnull Command command) {
        this.name = checkNotNull(name);
        this.command = checkNotNull(command);
    }

    public Command getCommand() {
        return this.command;
    }

    public String getName() {
        return this.name;
    }

    /**
     * Parse the command Line and return the requested command if exists.
     *
     * @param args the list of arguments. Cannot be null. The first argument (index [0]) is the command
     * @return the requested command
     * @throws IllegalArgumentException if a command has not been found
     */
    public static Command parse(@Nonnull String[] args) throws IllegalArgumentException {
        checkNotNull(args);

        String name = args[0];
        Command c = Commands.valueOf(name.toUpperCase(Locale.ENGLISH)).getCommand();
        LOG.debug("Looked up for '{}' and found {}", name, c);

        return c;
    }
}
