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

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import static org.junit.Assert.*;

public class AddCommandTest {
    @Test(expected = NullPointerException.class)
    public void parseCommandLineNullArgs() {
        new AddCommand().parseCommandLine(new PrintWriter(new ByteArrayOutputStream()), null);
    }

    @Test(expected = NullPointerException.class)
    public void parseCommandLineNullPW() {
        new AddCommand().parseCommandLine(null, new String[0]);
    }

    @Test
    public void parseCommandLine() {
        PrintWriter pw = new PrintWriter(new ByteArrayOutputStream());

        assertNull("both path and primary type are mandatory", new AddCommand().parseCommandLine(pw, new String[0]));
        assertNull("both path and primary type are mandatory", new AddCommand().parseCommandLine(pw, new String[]{"add"}));
        assertNull("both path and primary type are mandatory", new AddCommand().parseCommandLine(pw, new String[]{"add", "/path/to/node"}));

        AddCommand.Options options = null;
        options = new AddCommand().parseCommandLine(pw, new String[]{"add", "/path/to/node", "primaryType"});
        assertNotNull(options);
        assertEquals("path/to/node", options.path);
        assertEquals("primaryType", options.primaryType);

        options = new AddCommand().parseCommandLine(pw, new String[]{"add", "path/to/node", "primaryType"});
        assertNotNull(options);
        assertEquals("path/to/node", options.path);
        assertEquals("primaryType", options.primaryType);
    }
}
