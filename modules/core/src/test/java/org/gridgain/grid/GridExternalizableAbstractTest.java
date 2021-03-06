/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gridgain.grid;

import org.apache.ignite.marshaller.*;
import org.apache.ignite.marshaller.jdk.*;
import org.apache.ignite.marshaller.optimized.*;
import org.gridgain.testframework.junits.common.*;
import java.util.*;

/**
 * Base externalizable test class.
 */
public class GridExternalizableAbstractTest extends GridCommonAbstractTest {
    /**
     * @return Marshallers.
     */
    protected List<IgniteMarshaller> getMarshallers() {
        List<IgniteMarshaller> marshallers = new ArrayList<>();

        marshallers.add(new IgniteJdkMarshaller());
        marshallers.add(new IgniteOptimizedMarshaller());

        return marshallers;
    }
}
