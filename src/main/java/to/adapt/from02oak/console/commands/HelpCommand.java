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

import javax.annotation.Nonnull;
import java.io.PrintWriter;
import java.util.Arrays;

import static to.adapt.from02oak.console.commands.Commands.LEXICOGRAPHICALLY_ORDER;

class HelpCommand implements Command {
    @Override
    public int execute(@Nonnull PrintWriter out, @Nonnull String[] args) {
        Arrays.stream(Commands.values()).sorted(LEXICOGRAPHICALLY_ORDER).forEach(command -> {
            Commands c = ((Commands)command);
            out.printf("%s - [%s]\n", c.getName(), c.getCommand().getDescription());
        });

        return 0;
    }

    @Override
    public String getDescription() {
        return "Print the help screen";
    }

    @Override
    public boolean isExit() {
        return false;
    }
}
