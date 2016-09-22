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

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.adapt.from02oak.repository.Repository;

import javax.annotation.Nonnull;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.io.PrintWriter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * add a new node.
 *
 * usage: add /path/to/node jcr:primaryType
 */
class AddCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(AddCommand.class);

    class Options {
        String path;
        String primaryType;

        /**
         *
         * @param p the path.
         * @param t the primaryType
         */
        Options(String p, String t) {
            if (p.startsWith("/")) {
                this.path = p.substring(1);
            } else {
                this.path = p;
            }
            this.primaryType = t;
        }
    }

    @Override
    public int execute(@Nonnull PrintWriter out, @Nonnull String[] args) throws IOException {
        Options o = parseCommandLine(out, args);
        if (o == null) {
            return 1;
        }

        Repository repo = Repository.getRepo();
        Session s = null;
        try {
            s = repo.login(Repository.ADMIN);

            LOG.debug("Adding node {} of type {}", o.path, o.primaryType);
            s.getRootNode().addNode(o.path, o.primaryType);
            s.save();
        } catch (RepositoryException e) {
            LOG.error("Error while adding a node.", e);
            out.println("Error while adding node. " + e.getMessage());
            return 1;
        } finally {
            if (s != null) {
                s.logout();
            }
        }

        return 0;
    }

    /**
     * parse the command line and return the options
     * @param args Cannot be null.
     * @return options for the executing the command or {@code null} in case of errors
     */
    Options parseCommandLine(@Nonnull PrintWriter out, @Nonnull String[] args) {
        checkNotNull(args);
        checkNotNull(out);

        // better test on parameters would be awesome here!
        if (args.length < 3) {
            out.println("Error parsing the command line");
            out.println("Usage: add /path/to/node jcr:primaryType");
            return null;
        }

        return new Options(args[1], args[2]);
    }

    @Override
    public String getDescription() {
        return "Add a new node";
    }

    @Override
    public boolean isExit() {
        return false;
    }
}
