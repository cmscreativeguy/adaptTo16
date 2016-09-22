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

import org.junit.Test;
import to.adapt.from02oak.repository.Repository;
import to.adapt.from02oak.repository.TestRepository;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ListCommandTest {
    private final Command CMD = new ListCommand();

    @Test
    public void missingMandatoryPath() throws IOException {
        // initialising a test repository
        new TestRepository();

        StringWriter writer = null;
        PrintWriter pw = null;

        try {
            writer = new StringWriter();
            pw = new PrintWriter(writer);

            int code = CMD.execute(pw, new String[]{"ls"});
            assertEquals(1, code);
            assertTrue(writer.toString().contains(ListCommand.ERROR_CMD_LINE_PARSING));

            writer.getBuffer().setLength(0);
            assertEquals(0, CMD.execute(pw, new String[]{"ls", "/"}));
        } finally {
            if (pw != null ) { pw.close(); }
            if (writer != null) { writer.close(); }
        }
    }
}
