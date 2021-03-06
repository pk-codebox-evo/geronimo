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
 */package org.apache.geronimo.j2ee.deployment;

import javax.naming.Reference;
import javax.management.ObjectName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanData;

/**
 * @version $Rev$ $Date$
 */
public interface ResourceReferenceBuilder {

    Reference createResourceRef(String containerId, Class iface) throws DeploymentException;

    Reference createAdminObjectRef(String containerId, Class iface) throws DeploymentException;

    GBeanData locateActivationSpecInfo(GBeanData resourceAdapterModuleData, String messageListenerInterface) throws DeploymentException;

    GBeanData locateResourceAdapterGBeanData(GBeanData resourceAdapterModuleData) throws DeploymentException;

    GBeanData locateAdminObjectInfo(GBeanData resourceAdapterModuleData, String adminObjectInterfaceName) throws DeploymentException;

    GBeanData locateConnectionFactoryInfo(GBeanData resourceAdapterModuleData, String connectionFactoryInterfaceName) throws DeploymentException;
}
