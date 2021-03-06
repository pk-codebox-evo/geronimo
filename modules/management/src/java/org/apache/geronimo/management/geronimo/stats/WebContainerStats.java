/**
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.management.geronimo.stats;

import javax.management.j2ee.statistics.Stats;
import javax.management.j2ee.statistics.RangeStatistic;
import javax.management.j2ee.statistics.TimeStatistic;
import javax.management.j2ee.statistics.CountStatistic;

/**
 * Statistics exposed by a web container (for the container as a whole, not
 * a particular servlet/JSP/URL).
 *
 * todo: confirm the definitions of the Jetty stats included here; verify these are valid for Tomcat as well
 *
 * @version $Revision: 1.0$
 */
public interface WebContainerStats extends Stats {
    /**
     * Gets the number of connections currently open (as well as the min and
     * max since statistics gathering started).
     */
    RangeStatistic getOpenConnectionCount();

    /**
     * Gets the number of requests handled by a particular connection (as well
     * as the min and max since statistics gathering started).
     */
    RangeStatistic getConnectionRequestCount();

    /**
     * Gets the legnth of time that connections have been open (includes
     * figures across all connections open at present)
     */
    TimeStatistic getConnectionDuration();

    /**
     * Gets the number of errors that have been returned since statistics
     * gathering started.
     */
    CountStatistic getTotalErrorCount();

    /**
     * Gets the number of requests that have been processed since statistics
     * gathering started.
     */
    CountStatistic getTotalRequestCount();

    /**
     * Gets the number of requests being processed concurrently (as well
     * as the min and max since statistics gathering started).
     */
    RangeStatistic getActiveRequestCount();

    /**
     * Gets the legnth of time taken to process a request (includes
     * figures across all requests since statistics gathering started)
     */
    TimeStatistic getRequestDuration();
}
