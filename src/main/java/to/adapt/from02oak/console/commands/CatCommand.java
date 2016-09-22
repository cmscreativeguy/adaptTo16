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

import com.google.common.collect.Lists;
import joptsimple.OptionParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.adapt.from02oak.repository.Repository;

import javax.annotation.Nonnull;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

class CatCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(CatCommand.class);
    private final OptionParser PARSER = new OptionParser();

    @Override
    public int execute(@Nonnull PrintWriter out, @Nonnull String[] args) throws IOException {
        List<String> paths = analyseCommandLine(args);
        if (paths == null) {
            out.println("Error parsing the command line");
            printHelp(out);
            return 1;
        }

        Repository repo = Repository.getRepo();
        Session session = null;

        try {
            session = repo.login(Repository.ADMIN);
            Node n = session.getNode(paths.get(0));
            n.getProperties().forEachRemaining(o -> {
                Property p = (Property)o;
                try {
                    out.printf("- %s: %s%n", p.getName(), p.isMultiple() ? "[" +
                            Arrays.stream(p.getValues()).map(value -> {
                                String s = null;
                                try {
                                    s = value.getString();
                                } catch (RepositoryException e) {
                                    LOG.error("Error printing multi-value", e);
                                }
                                return s;
                            }).collect(Collectors.joining(", "))
                            + "]"
                            : p.getValue());
                } catch (RepositoryException e) {
                    LOG.error("Error while viewving path.", e);
                    out.println("Error while viewing path. " + e.getMessage());
                }
            });
        } catch (RepositoryException e) {
            LOG.error("Error while viewving path.", e);
            out.println("Error while viewing path. " + e.getMessage());

        }
        return 0;
    }

    /**
     * validate the command line and return the options for further works
     *
     * @param args cannot be null.
     * @return null in case of errors.
     */
    List<String> analyseCommandLine(@Nonnull String[] args) {
        checkNotNull(args);
        List<?> nonOptions = PARSER.parse(args).nonOptionArguments();

        if (nonOptions.isEmpty()) {
            return null;
        }

        List<String> l = Lists.newArrayList();
        nonOptions.forEach(o -> {l.add(o.toString());});

        // removing the first item as it will be the command line.
        l.remove(0);

        return l.isEmpty() ? null : l;
    }

    @Override
    public String getDescription() {
        return "Print the content of a node";
    }

    @Override
    public boolean isExit() {
        return false;
    }

    void printHelp(@Nonnull PrintWriter out) {
        checkNotNull(out);
        out.println("cat /path/to/view");
    }
}
