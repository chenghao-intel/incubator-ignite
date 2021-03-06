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

package org.apache.ignite.spi.discovery;

import java.util.*;

/**
 * Handler for initial data exchange between GridGain nodes. Data exchange
 * is initiated by a new node when it tries to join topology and finishes
 * before it actually joins.
 */
public interface DiscoverySpiDataExchange {
    /**
     * Collects data from all components. This method is called both
     * on new node that joins topology to transfer its data to existing
     * nodes and on all existing nodes to transfer their data to new node.
     *
     * @param nodeId ID of new node that joins topology.
     * @return Collection of discovery data objects from different components.
     */
    public List<Object> collect(UUID nodeId);

    /**
     * Notifies discovery manager about data received from remote node.
     *
     * @param data Collection of discovery data objects from different components.
     */
    public void onExchange(List<Object> data);
}
