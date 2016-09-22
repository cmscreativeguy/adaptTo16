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

package to.adapt.from02oak.console;

import com.google.common.base.Strings;
import jline.console.ConsoleReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.adapt.from02oak.console.commands.Command;
import to.adapt.from02oak.console.commands.Commands;
import to.adapt.from02oak.repository.Repository;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintWriter;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

public class Console {
    private static final Logger LOG = LoggerFactory.getLogger(Console.class);

    public void start() throws IOException {
        ConsoleReader reader;
        PrintWriter out = null;
        String line;

        try {

            // initialise the console
            reader = new ConsoleReader();
            reader.setPrompt("$ ");
            out = new PrintWriter(reader.getOutput());

            header(out);

            // forcing repository initialisation
            out.println("Initialising repository");
            if (Repository.getRepo() == null) {
                out.println("Error while initialising the repository. Quitting.");
            } else {
                out.println("done");
                out.println("");
                boolean exit = false;
                // interactive inputs
                while (!exit && (line = reader.readLine()) != null) {
                    if (!isNullOrEmpty(line)) {
                        String[] a = line.split(" ");
                        Command c;
                        try {
                            c = Commands.parse(a);
                            c.execute(out,a);
                            exit = c.isExit();
                        } catch (IllegalArgumentException e) {
                            LOG.error("Command not found. {} ", a, e);
                            out.println("Command not found. Try `help`");
                        } catch (IOException e) {
                            LOG.error("Error executing", e);
                            out.printf("Error executing. %s\n", e.getMessage());
                        }
                    }
                }
            }

        } catch (IOException e) {
            LOG.error("", e);
        } finally {
            // ensuring the proper shutdown
            if (out != null) { out.println("Shutting down the repository"); }
            Repository.getRepo().close();
        }
        if (out != null) { out.println("Bye!"); }
    }

    private static void header(@Nonnull PrintWriter out) {
        checkNotNull(out);

        out.println("----------------------------------------------------------------------");
        out.println("            From0 to Oak in 30. Type `help` for help!                 ");
        out.println("----------------------------------------------------------------------");
        out.println();
    }
}
