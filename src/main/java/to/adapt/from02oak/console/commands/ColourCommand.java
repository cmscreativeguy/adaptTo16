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
import javax.jcr.query.*;
import java.io.IOException;
import java.io.PrintWriter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * query the repository for nodes with the provided colour
 *
 * Usage:
 *  colour red
 */
public class ColourCommand  implements Command{
    private static final Logger LOG = LoggerFactory.getLogger(ColourCommand.class);

    @Override
    public int execute(@Nonnull PrintWriter out, @Nonnull String[] args) throws IOException {
        checkNotNull(out);
        checkNotNull(args);

        if (args.length < 2) {
            out.println("Error parsing the command line. Provide a colour to search for");
            out.println("Usage: colour <desired-colour>");
            return 1;
        }

        String colour = args[1];
        String statement = String.format("SELECT * FROM [nt:base] WHERE colour = '%s'", colour);
        Session session = null;

        try {
            session = Repository.getRepo().login(Repository.ADMIN);
            QueryManager qm =  session.getWorkspace().getQueryManager();
            Query q = qm.createQuery(statement, Query.JCR_SQL2);
            QueryResult results = q.execute();
            RowIterator rows = results.getRows();

            if (rows.hasNext()) {
                rows.forEachRemaining(o -> {
                    Row r = (Row)o;
                    try {
                        out.printf("  - %s%n", r.getPath());
                    } catch (RepositoryException e) {
                        LOG.error("Error looping through nodes", e);
                        out.println("Error looping through nodes. " + e.getMessage());
                    }
                });
            } else {
                out.printf("No nodes found for colour: %s%n", colour);
            }
        } catch (RepositoryException e) {
            LOG.error("Error searching for colours", e);
            out.println("Error while searching for colour. " + e.getMessage());
        } finally {
            if (session != null) {
                session.logout();
            }
        }


        return 0;
    }

    @Override
    public String getDescription() {
        return "Search for all the nodes with a specific `colour`";
    }

    @Override
    public boolean isExit() {
        return false;
    }
}
