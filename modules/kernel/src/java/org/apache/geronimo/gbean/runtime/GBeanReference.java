/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.gbean.runtime;

import java.util.Set;

import org.apache.geronimo.gbean.GReferenceInfo;

/**
 * @version $Rev$ $Date$
 */
public interface GBeanReference {
    String getName();

    GReferenceInfo getReferenceInfo();

    Class getReferenceType();

    Class getProxyType();

    Set getPatterns();

    void setPatterns(Set patterns);

    void online();

    void offline();

    boolean start();

    void stop();

    Object getProxy();

    void inject(Object target) throws Exception;
}
