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


package to.adapt.from02oak.repository.initialisers;

import org.apache.jackrabbit.oak.spi.lifecycle.RepositoryInitializer;
import org.apache.jackrabbit.oak.spi.state.NodeBuilder;

import javax.annotation.Nonnull;
import java.util.Random;

import static org.apache.jackrabbit.JcrConstants.JCR_PRIMARYTYPE;
import static org.apache.jackrabbit.oak.plugins.nodetype.NodeTypeConstants.NT_OAK_UNSTRUCTURED;

public class BunchOfColours implements RepositoryInitializer {
    private static final String UNITED_COLOURS = "unitedcolours";

    private enum COLOURS {
        RED, GREEN, BLUE;

        static Random r = new Random();
        static COLOURS[] cc = COLOURS.values();

        static String getRandomColour() {
            return cc[r.nextInt(cc.length)].toString().toLowerCase();
        }
    }

    @Override
    public void initialize(@Nonnull NodeBuilder builder) {
        if (builder.getChildNode(UNITED_COLOURS).exists()) {
            // nodes are already there.
            return;
        }
        NodeBuilder unitedColours = builder.child(UNITED_COLOURS);
        for (int i = 0; i<100; i++) {
            unitedColours.child(String.format("n%03d", i))
                    .setProperty(JCR_PRIMARYTYPE, NT_OAK_UNSTRUCTURED)
                    .setProperty("colour", COLOURS.getRandomColour());
        }
    }
}
