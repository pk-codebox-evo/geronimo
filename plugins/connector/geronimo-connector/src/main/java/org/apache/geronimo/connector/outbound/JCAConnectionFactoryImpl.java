/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.connector.outbound;

import java.util.Hashtable;

import javax.management.ObjectName;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.impl.InvalidObjectNameException;
import org.apache.geronimo.kernel.ObjectNameUtil;
import org.apache.geronimo.management.geronimo.JCAConnectionFactory;
import org.apache.geronimo.management.geronimo.JCAManagedConnectionFactory;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = NameFactory.JCA_CONNECTION_FACTORY)
public class JCAConnectionFactoryImpl implements JCAConnectionFactory {
    private final String objectName;
    private final JCAManagedConnectionFactory managedConnectionFactory;

    public JCAConnectionFactoryImpl(@ParamSpecial(type = SpecialAttributeType.objectName) String objectName,
                                    @ParamReference(name = "JCAManagedConnectionFactory", namingType = NameFactory.JCA_MANAGED_CONNECTION_FACTORY) JCAManagedConnectionFactory managedConnectionFactory) {
        ObjectName myObjectName = ObjectNameUtil.getObjectName(objectName);
        verifyObjectName(myObjectName);

        this.objectName = objectName;
        this.managedConnectionFactory = managedConnectionFactory;
    }

    public String getManagedConnectionFactory() {
        return managedConnectionFactory.getObjectName();
    }

    public JCAManagedConnectionFactory getManagedConnectionFactoryInstance() {
        return managedConnectionFactory;
    }

    public String getObjectName() {
        return objectName;
    }

    public boolean isStateManageable() {
        return false;
    }

    public boolean isStatisticsProvider() {
        return false;
    }

    public boolean isEventProvider() {
        return false;
    }

    /**
     * ObjectName must match this pattern:
     * <p/>
     * domain:j2eeType=JCAConnectionFactory,name=MyName,J2EEServer=MyServer,JCAResource=MyJCAResource
     */
    private void verifyObjectName(ObjectName objectName) {
        if (objectName.isPattern()) {
            throw new InvalidObjectNameException("ObjectName can not be a pattern", objectName);
        }
        Hashtable keyPropertyList = objectName.getKeyPropertyList();
        if (!"JCAConnectionFactory".equals(keyPropertyList.get("j2eeType"))) {
            throw new InvalidObjectNameException("JCAConnectionFactory object name j2eeType property must be 'JCAConnectionFactory'", objectName);
        }
        if (!keyPropertyList.containsKey("name")) {
            throw new InvalidObjectNameException("JCAConnectionFactory object must contain a name property", objectName);
        }
        if (!keyPropertyList.containsKey("J2EEServer")) {
            throw new InvalidObjectNameException("JCAConnectionFactory object name must contain a J2EEServer property", objectName);
        }
        if (!keyPropertyList.containsKey("JCAResource")) {
            throw new InvalidObjectNameException("JCAResource object name must contain a JCAResource property", objectName);
        }
//        if (keyPropertyList.size() != 4) {
//            throw new InvalidObjectNameException("JCAConnectionFactory object name can only have j2eeType, name, JCAResource, and J2EEServer properties", objectName);
//        }
    }
}