/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.omg.IOP.IOR;


/**
 * A GBean that provides an example interop
 *
 * @version $Rev$ $Date$
 */
public class InteropGBean implements GBeanLifecycle {

    private final Log log = LogFactory.getLog(InteropGBean.class);

    private IOR ior;

    private Properties  properties;
    private String      strprop;
    private String      name;

    /**
     * Construct an instance of InteropGBean
     *
     * @param strprop some strprop
     */
    public InteropGBean(String name, String strprop, Properties properties) {
        this.name = name;
        this.strprop = strprop;
        this.properties = (properties == null ? new Properties() : properties);
    }

    /**
     * Returns the strprop.
     */
    public String getAStrProp() {
        return strprop;
    }

    /**
     * Sets the strprop
     *
     * @param strprop the strprop
     */
    public void setAStrProp(String strprop) {
        this.strprop = strprop;
    }

    /**
     * Returns the Properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Sets the properties
     *
     * @param properties the props.
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * Returns the object name of this protocol GBean
     */
    public String getName() {
        return name;
    }

    /**
     * Add the overrides from the member variables to the properties file.
     */
    public void echo(String msg) {
        log.info(getName() + ": Echo " + msg);
    }

    /*
     * Interface :: GBeanLifecycle
     */

    public void doStart() throws Exception {
        log.debug("Started " + getName());
    }

    public void doStop() throws Exception {
        log.debug("Stopped " + getName());
    }

    public void doFail() {
        log.warn("Failed " + getName());
    }

    /*
     * GBeanInfo
     */

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(InteropGBean.class);

        infoFactory.addAttribute("name", String.class, true);
        infoFactory.addAttribute("strprop", String.class, true);
        infoFactory.addAttribute("properties", Properties.class, true);

        infoFactory.addOperation("echo", new Class[]{String.class});

        infoFactory.setConstructor(new String[]{"name", "strprop", "properties"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
