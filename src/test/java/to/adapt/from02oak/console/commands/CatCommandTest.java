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

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

public class CatCommandTest {
    @Test
    public void analyseCommandLine() {
        assertNull(new CatCommand().analyseCommandLine(new String[]{"cat"}));
        assertNull(new CatCommand().analyseCommandLine(new String[0]));

        List<String> opts = new CatCommand().analyseCommandLine(new String[]{"cat", "/mickey/mouse"});
        assertNotNull(opts);
        assertThat(opts, containsInAnyOrder(new String[]{"/mickey/mouse"}));
    }

    @Test(expected = NullPointerException.class)
    public void analyseCommandLineNullArgs() {
        new CatCommand().analyseCommandLine(null);
    }
}
