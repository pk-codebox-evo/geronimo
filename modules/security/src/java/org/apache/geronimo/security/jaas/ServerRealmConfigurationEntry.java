/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
package org.apache.geronimo.security.jaas;

import java.util.Properties;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.Kernel;

/**
 * Creates a LoginModule configuration that will connect a server-side
 * component to a security realm.  The same thing could be done with a
 * LoginModuleGBean and a DirectConfigurationEntry, but this method saves some
 * configuration effort.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class ServerRealmConfigurationEntry implements GBeanLifecycle {
    private String applicationConfigName;
    private String realmName;
    private Kernel kernel;

    public ServerRealmConfigurationEntry() {
        // just for use by GBean infrastructure
    }

    public ServerRealmConfigurationEntry(String applicationConfigName, String realmName, Kernel kernel) {
        this.applicationConfigName = applicationConfigName;
        this.realmName = realmName;
        if(applicationConfigName == null || realmName == null) {
            throw new IllegalArgumentException("applicationConfigName and realmName are required");
        }
        if(applicationConfigName.equals(realmName)) {
            throw new IllegalArgumentException("applicationConfigName must be different than realmName (there's an automatic entry using the same name as the realm name, so you don't need a ServerRealmConfigurationEntry if you're just going to use that!)");
        }
        this.kernel = kernel;
    }

    public void doStart() throws WaitingException, Exception {
        Properties options = new Properties();
        options.put("realm", realmName);
        options.put("kernel", kernel.getKernelName());
        JaasLoginModuleConfiguration entry = new JaasLoginModuleConfiguration(applicationConfigName, JaasLoginCoordinator.class.getName(), LoginModuleControlFlag.REQUIRED, options, true);
        GeronimoLoginConfiguration.register(entry);
    }

    public void doStop() throws WaitingException, Exception {
        GeronimoLoginConfiguration.unRegister(applicationConfigName);
    }

    public void doFail() {
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(ServerRealmConfigurationEntry.class);
        infoFactory.addAttribute("applicationConfigName", String.class, true);
        infoFactory.addAttribute("realmName", String.class, true);
        infoFactory.addAttribute("kernel", Kernel.class, false);

        infoFactory.setConstructor(new String[]{"applicationConfigName", "realmName", "kernel"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
