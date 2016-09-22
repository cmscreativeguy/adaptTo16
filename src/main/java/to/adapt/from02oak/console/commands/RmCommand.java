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
import to.adapt.from02oak.repository.Repository;

import javax.annotation.Nonnull;
import javax.jcr.Session;
import java.io.IOException;
import java.io.PrintWriter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * remove a specific node and subnodes
 *
 * usage: rm /path/to/node
 */
class RmCommand implements Command {
    private static final Logger LOG = LoggerFactory.getLogger(RmCommand.class);

    @Override
    public int execute(@Nonnull PrintWriter out, @Nonnull String[] args) throws IOException {
        String p = parseCommandLine(out, args);
        if (p == null) {
            return 1;
        }

        Session s = null;

        try {
            s = Repository.getRepo().login(Repository.ADMIN);
            s.getNode(p).remove();
            s.save();
        } catch (Exception e) {
            LOG.error("Error while deleting node", e);
            out.println("Error while deleting node. " + e.getMessage());
            return 1;
        } finally {
            if (s != null) {
                s.logout();
            }
        }

        return 0;
    }

    String parseCommandLine(@Nonnull PrintWriter out, @Nonnull String[] args) {
        checkNotNull(out);
        checkNotNull(args);

        if (args.length < 2) {
            out.println("Error parsing the command line");
            out.println("Usage: rm /path/to/node");
            return null;
        }
        return args[1];
    }

    @Override
    public String getDescription() {
        return "remove a node and all the subnodes";
    }

    @Override
    public boolean isExit() {
        return false;
    }
}
