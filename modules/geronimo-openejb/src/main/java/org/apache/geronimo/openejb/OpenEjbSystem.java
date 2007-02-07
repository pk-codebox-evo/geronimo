/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.openejb;

import java.io.IOException;
import java.util.Properties;
import javax.naming.NamingException;

import org.apache.openejb.Container;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.config.ClientModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.assembler.classic.ClientInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.spi.ContainerSystem;

/**
 * @version $Rev$ $Date$
 */
public interface OpenEjbSystem {
    ContainerSystem getContainerSystem();

    Container createContainer(Class<? extends ContainerInfo> type, String serviceId, Properties declaredProperties, String providerId) throws OpenEJBException;

    ClientInfo configureApplication(ClientModule clientModule) throws OpenEJBException;

    EjbJarInfo configureApplication(EjbModule ejbModule) throws OpenEJBException;

    void createClient(ClientInfo clientInfo, ClassLoader classLoader) throws NamingException, IOException, OpenEJBException;

    void createEjbJar(EjbJarInfo ejbJarInfo, ClassLoader classLoader) throws NamingException, IOException, OpenEJBException;

    DeploymentInfo getDeploymentInfo(String deploymentId);

    void removeEjbJar(EjbJarInfo ejbJarInfo, ClassLoader classLoader) throws UndeployException, NoSuchApplicationException;

    AppInfo configureApplication(AppModule appModule) throws OpenEJBException;
}
