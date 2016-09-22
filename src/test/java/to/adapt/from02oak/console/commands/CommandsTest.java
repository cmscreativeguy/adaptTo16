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

import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CommandsTest {
    @Test(expected = NullPointerException.class)
    public void nonNullArgs() {
        Commands.parse(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseNonExisting() {
        Commands.parse(new String[]{"nonExistingCommand"});
    }

    @Test
    public void parseCorrect() {
        Commands[] cc = Commands.values();

        assertNotNull(cc);
        assertThat(cc, is(not(emptyArray())));

        assertNotNull(Commands.parse(new String[]{cc[0].getName()}));
    }

    @Test(expected = NullPointerException.class)
    public void comparatorNullArgument1() {
        Commands.LEXICOGRAPHICALLY_ORDER.compare(null, Commands.HELP);
    }

    @Test(expected = NullPointerException.class)
    public void comparatorNullArgument2() {
        Commands.LEXICOGRAPHICALLY_ORDER.compare(Commands.HELP, null);
    }

    @Test
    public void comparatorCompare() {
        assertTrue(Commands.LEXICOGRAPHICALLY_ORDER.compare(Commands.HELP, Commands.EXIT) > 0);
        assertTrue(Commands.LEXICOGRAPHICALLY_ORDER.compare(Commands.HELP, Commands.HELP) == 0);
        assertTrue(Commands.LEXICOGRAPHICALLY_ORDER.compare(Commands.EXIT, Commands.HELP) < 0);
    }
}
