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

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class UpCommandTest {
    @Test(expected = NullPointerException.class)
    public void parseCommandLineMissingPW() {
        new UpCommand().parseCommandLine(null, new String[0]);
    }

    @Test(expected = NullPointerException.class)
    public void parseCommandLineMissingArgs() {
        new UpCommand().parseCommandLine(new PrintWriter(new ByteArrayOutputStream()), null);
    }

    @Test
    public void parseCommandLinePathArgument() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);

        assertEquals(0, sw.getBuffer().length());
        assertNull("Empty args is an error", new UpCommand().parseCommandLine(pw, new String[]{}));
        assertTrue("We should have had an error message", sw.getBuffer().length() > 0);

        sw.getBuffer().setLength(0);
        assertNull("Missing path", new UpCommand().parseCommandLine(pw, new String[]{"up"}));
        assertTrue("We should have had an error message", sw.getBuffer().length() > 0);
    }

    @Test
    public void parseCommandLineDeleteOperation() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        assertNull(new UpCommand().parseCommandLine(pw, new String[]{"up", "/mickey/mouse", "--delete"}));
        assertTrue(sw.getBuffer().length() > 0);

        UpCommand.Instruction instruction = new UpCommand()
                .parseCommandLine(pw, new String[]{"up", "/mickey/mouse", "--delete", "propertyFoo"});
        assertNotNull(instruction);
        assertEquals(UpCommand.Operation.DELETE, instruction.op);
        assertEquals("propertyFoo", instruction.propertyName);
        assertEquals("/mickey/mouse", instruction.path);
    }

    @Test
    public void parseCommandLineEditOperation() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        assertNull(new UpCommand().parseCommandLine(pw, new String[]{"up", "/mickey/mouse", "--edit"}));
        assertTrue(sw.getBuffer().length() > 0);

        sw.getBuffer().setLength(0);
        assertNull(new UpCommand().parseCommandLine(pw, new String[]{"up", "/mickey/mouse", "--edit", "propertyFoo"}));
        assertTrue(sw.getBuffer().length() > 0);

        UpCommand.Instruction i = new UpCommand()
                .parseCommandLine(pw, new String[]{"up", "/mickey/mouse", "--edit", "propertyFoo", "helloValue"});
        assertNotNull(i);
        assertEquals(UpCommand.Operation.EDIT, i.op);
        assertEquals("/mickey/mouse", i.path);
        assertEquals("propertyFoo", i.propertyName);
        assertNotNull(i.values);
        assertEquals("helloValue", i.values.get(0));

        i = new UpCommand()
                .parseCommandLine(pw, new String[]{"up", "/mickey/mouse", "--edit", "propertyFoo", "value0, value1"});
        assertNotNull(i);
        assertEquals(UpCommand.Operation.EDIT, i.op);
        assertEquals("/mickey/mouse", i.path);
        assertEquals("propertyFoo", i.propertyName);
        assertNotNull(i.values);
        assertEquals(2, i.values.size());
        assertEquals("value0", i.values.get(0));
        assertEquals("value1", i.values.get(1));
    }

    @Test
    public void parseCommandLineMissingOp() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        assertNull(new UpCommand().parseCommandLine(pw, new String[]{"up", "/mickey/mouse"}));
        assertTrue(sw.getBuffer().length() > 0);
    }
}
