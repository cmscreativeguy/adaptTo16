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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.adapt.from02oak.repository.Repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static to.adapt.from02oak.repository.Repository.ADMIN;

class ListCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(ListCommand.class);
    final static String ERROR_CMD_LINE_PARSING = "Error parsing the command line.";

    @Override
    public int execute(@Nonnull PrintWriter out, @Nonnull String[] args) throws IOException {
        List<String> options = analyseCommandLine(out, args);
        if (options == null) {
            out.println(ERROR_CMD_LINE_PARSING);
            out.println("Usage: ls /path/to/node");
            return 1;
        }

        String p = options.get(0);
        Repository repo = Repository.getRepo();
        Session session = null;

        try {
            session = repo.login(ADMIN);
            Node node = session.getNode(p);
            NodeIterator children = node.getNodes();
            if (!children.hasNext()) {
                out.println("-- No children");
            } else {
                children.forEachRemaining(o -> {
                    Node n = (Node)o;
                    try {
                        out.println(n.getName());
                    } catch (RepositoryException e) {
                        LOG.error("Error printing node. ", e);
                        out.println("Error printing node");
                    }
                });
            }
        } catch (RepositoryException e) {
            LOG.error("Error while listing path. ", e);
            out.printf("Error while listing path %s - %s%n", p, e.getMessage());
            return 1;
        }

        return 0;
    }

    @Override
    public String getDescription() {
        return "list the nodes for the provided path";
    }

    @Override
    public boolean isExit() {
        return false;
    }

    /**
     * analyse the command line and return the computed options if all right. In case of errors it will return {@code null}.
     *
     * @param out
     * @param args
     * @return the options or {@code null} in case of errors on the command line
     * @throws IOException
     */
    @Nullable
    private List<String> analyseCommandLine(@Nonnull PrintWriter out, @Nonnull String[]  args) throws IOException {
        checkNotNull(out);
        checkNotNull(args);

        // we're expecting to have `ls <path to list>`
        if (args.length < 2) {
            return null;
        }

        // removing item at 0 as it's the command itself
        List<String> l = Lists.newArrayList();
        for (int i = 1; i < args.length; i++) {
            l.add(args[i]);
        }

        return l;
    }
}