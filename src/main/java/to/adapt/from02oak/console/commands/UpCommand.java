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

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.adapt.from02oak.repository.Repository;

import javax.annotation.Nonnull;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * update a node
 *
 * Usage:
 *
 *      // delete a property
 *      up /path/to/node --delete propertyName
 *
 *      // add or modify an existing one
 *      up /path/to/node --edit propertyName value/[multi,value]
 */
class UpCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(UpCommand.class);
    private OptionParser parser = new OptionParser();
    private OptionSpec<String> edit = parser.accepts("edit", "add or modify a property")
            .withRequiredArg()
            .describedAs("propertyName value/[multi,value,\"text,multi\"]")
            .ofType(String.class);
    private OptionSpec<String> delete = parser.accepts("delete", "delete a property")
            .withRequiredArg()
            .describedAs("propertyName")
            .ofType(String.class);

    enum Operation {
        DELETE, EDIT
    };

    class Instruction {
        Operation op;
        String path;
        String propertyName;
        List<String> values;
    }

    @Override
    public int execute(@Nonnull PrintWriter out, @Nonnull String[] args) throws IOException {
        Instruction in = parseCommandLine(out, args);
        if (in == null) {
            return 1;
        }

        Session session = null;
        Node n;
        try {
            switch (in.op) {
                case DELETE:
                    session = Repository.getRepo().login(Repository.ADMIN);
                    n = session.getNode(in.path);
                    n.getProperty(in.propertyName).remove();
                    session.save();
                    break;
                case EDIT:
                    session = Repository.getRepo().login(Repository.ADMIN);
                    n = session.getNode(in.path);
                    if (in.values.size() == 1) {
                        // single value property
                        n.setProperty(in.propertyName, in.values.get(0));
                    } else {
                        // multi-value property
                        n.setProperty(in.propertyName, in.values.toArray(new String[0]));
                    }
                    session.save();
                    break;
            }
        } catch (RepositoryException e) {
            LOG.error("Error updating node", e);
            out.println("Error updating node. " + e.getMessage());
            return 1;
        } finally {
            if (session != null) {
                session.logout();;
            }
        }

        return 0;
    }

    /**
     * analise the command line and return the equivalent {@code Instruction}.
     *
     * @param out where to redirect any output message. Cannot be null
     * @param args passed in command line. Cannot be null.
     * @return the evaluated instruction or null in case of errors.
     */
    Instruction parseCommandLine(@Nonnull PrintWriter out, @Nonnull String[] args) {
        checkNotNull(out);
        checkNotNull(args);

        OptionSet options = null;
        Instruction instruction = null;

        try {
            options = parser.parse(args);
        } catch (Exception e) {
            LOG.error("Error occured while parsing the command line", e);
            out.println("Error parsing the command line. " + e.getMessage());
            printHelp(out);
            return null;
        }

        // converting undefined immutable list to mutable String. Hooray for streams!!
        List<String> nonOptions = options.nonOptionArguments().stream().map(o -> {
            return o.toString();
        }).collect(Collectors.<String>toList());


        if (!nonOptions.isEmpty()) {
            // first item should always be "up"
            nonOptions.remove(0);
        }
        if (nonOptions.isEmpty()) {
            LOG.error("Error parsing the command line. Missing node path.");
            out.println("Error parsing the command line. Missing node path.");
            printHelp(out);
            return null;
        }

        // getting the path
        String p = nonOptions.remove(0);

        if (options.has(delete)) {
            instruction = new Instruction();
            instruction.op = Operation.DELETE;
            instruction.propertyName = options.valueOf(delete);
            instruction.path = p;
        } else if (options.has(edit)) {
            if (nonOptions.isEmpty()) {
                LOG.error("Error parsing the command line. Missing property values for edit operation");
                out.println("Error parsing the command line. Missing property values for edit operation");
                printHelp(out);
                return null;
            }

            instruction = new Instruction();
            instruction.op = Operation.EDIT;
            instruction.path = p;
            instruction.propertyName = options.valueOf(edit);
            // trimming out values
            instruction.values = Arrays.stream(nonOptions.get(0).split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
        } else {
            out.println("Error parsing the command line. Missing or non valid operation");
            printHelp(out);
        }

        return instruction;
    }

    private void printHelp(@Nonnull PrintWriter pw) {
        checkNotNull(pw);

        pw.println("\nUsage: up <option> [option params]\n");

        try {
            parser.printHelpOn(pw);
        } catch (IOException e) {
            LOG.error("Error printing help", e);
            pw.write("Error while printing help screen.");
        }
    }
    @Override
    public String getDescription() {
        return "Update a node";
    }

    @Override
    public boolean isExit() {
        return false;
    }
}
