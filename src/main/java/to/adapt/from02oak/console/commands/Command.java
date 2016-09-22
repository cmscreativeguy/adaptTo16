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
import java.io.IOException;
import java.io.PrintWriter;

/**
 * an instance of an executable command.
 */
public interface Command {
    Command NO_OP = new Command() {
        @Override
        public int execute(@Nonnull PrintWriter out, @Nonnull String[] args) {
            return 0;
        }

        @Override
        public String getDescription() {
            return "A NoOp command";
        }

        @Override
        public boolean isExit() {
            return false;
        }
    };

    /**
     * execute the command
     *
     * @param out where to output any message. Cannot be null.
     * @param args the list of args. Cannot be null
     *
     * @return the exit code. {@code 0} all ok, something different from {@code 0} errors occured
     */
    int execute(@Nonnull PrintWriter out, @Nonnull String[]args) throws IOException;

    /**
     * @return a short description of the command itself. It will be printed out in the help screen.
     */
    String getDescription();

    /**
     *
     * @return {@code true} if the command should exit after its execution
     */
    boolean isExit();
}
