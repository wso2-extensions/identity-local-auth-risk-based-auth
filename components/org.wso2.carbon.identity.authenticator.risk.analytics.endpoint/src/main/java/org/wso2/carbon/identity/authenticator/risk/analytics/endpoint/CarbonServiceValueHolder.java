/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.authenticator.risk.analytics.endpoint;

import org.wso2.carbon.event.stream.core.EventStreamService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holder object to hold references which are needed by multiple classes.
 */
public class CarbonServiceValueHolder {
    private static EventStreamService eventstreamservice;
    private static Map<String, ResultContainer> resultContainerMap = new ConcurrentHashMap<>();

    private CarbonServiceValueHolder() {
        //DO nothing
    }

    public static EventStreamService getEventstreamservice() {
        return eventstreamservice;
    }

    public static void setEventstreamservice(EventStreamService eventstreamservice) {
        CarbonServiceValueHolder.eventstreamservice = eventstreamservice;
    }

    public static Map<String, ResultContainer> getResultContainerMap() {
        return resultContainerMap;
    }

    public static void setResultContainerMap(Map<String, ResultContainer> resultContainerMap) {
        CarbonServiceValueHolder.resultContainerMap = resultContainerMap;
    }
}
