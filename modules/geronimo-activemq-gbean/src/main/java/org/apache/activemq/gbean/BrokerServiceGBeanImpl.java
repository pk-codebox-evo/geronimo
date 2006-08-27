/**
 *
 * Copyright 2005-2006 The Apache Software Foundation
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
package org.apache.activemq.gbean;

import java.net.URI;

import javax.sql.DataSource;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.store.DefaultPersistenceAdapterFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.connector.outbound.ConnectionFactorySource;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.management.geronimo.JMSManager;
import org.apache.geronimo.management.geronimo.NetworkConnector;
import org.apache.geronimo.system.serverinfo.ServerInfo;


/**
 * Default implementation of the ActiveMQ Message Server
 *
 * @version $Revision$
 */
public class BrokerServiceGBeanImpl implements GBeanLifecycle, BrokerServiceGBean {

    private Log log = LogFactory.getLog(getClass().getName());

    private String brokerName;
    private String brokerUri;
    private BrokerService brokerService;
    private ServerInfo serverInfo;
    private String dataDirectory;
    private ConnectionFactorySource dataSource;
    private ClassLoader classLoader;
    private String objectName;
    private JMSManager manager;

    public BrokerServiceGBeanImpl() {
    }
    
    public synchronized BrokerService getBrokerContainer() {
        return brokerService;
    }

    public synchronized void doStart() throws Exception {
        	ClassLoader old = Thread.currentThread().getContextClassLoader();
        	Thread.currentThread().setContextClassLoader(getClassLoader());
        	try {
    	        if (brokerService == null) {
    	            brokerService = createContainer();
    	        }
                DefaultPersistenceAdapterFactory persistenceFactory = (DefaultPersistenceAdapterFactory) brokerService.getPersistenceFactory();
                persistenceFactory.setDataDirectory(serverInfo.resolve(dataDirectory));
                persistenceFactory.setDataSource((DataSource) dataSource.$getResource());
                brokerService.start();
        	} finally {
            	Thread.currentThread().setContextClassLoader(old);
        	}
    }

    protected BrokerService createContainer() throws Exception {
        if( brokerUri!=null ) {
            BrokerService answer = BrokerFactory.createBroker(new URI(brokerUri));
            brokerName = answer.getBrokerName();
            return answer;
        } else {
            BrokerService answer = new BrokerService();
            answer.setBrokerName(brokerName);
            return answer;
        }
    }

    public synchronized void doStop() throws Exception {
        if (brokerService != null) {
            BrokerService temp = brokerService;
            brokerService = null;
            temp.stop();
        }
    }

    public synchronized void doFail() {
        if (brokerService != null) {
            BrokerService temp = brokerService;
            brokerService = null;
            try {
                temp.stop();
            } catch (Exception e) {
                log.info("Caught while closing due to failure: " + e, e);
            }
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder("ActiveMQ Message Broker", BrokerServiceGBeanImpl.class, "JMSServer");
        infoBuilder.addReference("serverInfo", ServerInfo.class);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);
        infoBuilder.addAttribute("brokerName", String.class, true);
        infoBuilder.addAttribute("brokerUri", String.class, true);
        infoBuilder.addAttribute("dataDirectory", String.class, true);
        infoBuilder.addReference("dataSource", ConnectionFactorySource.class);
        infoBuilder.addAttribute("objectName", String.class, false);
        infoBuilder.addReference("manager", JMSManager.class);
        infoBuilder.addInterface(BrokerServiceGBean.class);
        // infoFactory.setConstructor(new String[]{"brokerName, brokerUri"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

	/**
	 * @return Returns the brokerName.
	 */
	public String getBrokerName() {
		return brokerName;
	}

    public String getBrokerUri() {
        return brokerUri;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public void setBrokerUri(String brokerUri) {
        this.brokerUri = brokerUri;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public String getDataDirectory() {
        return dataDirectory;
    }

    public void setDataDirectory(String dataDir) {
        this.dataDirectory = dataDir;
    }

    public ConnectionFactorySource getDataSource() {
        return dataSource;
    }

    public void setDataSource(ConnectionFactorySource dataSource) {
        this.dataSource = dataSource;
    }

    public String getObjectName() {
        return objectName;
    }

    public boolean isStateManageable() {
        return true;
    }

    public boolean isStatisticsProvider() {
        return false; // todo: return true once stats are integrated
    }

    public boolean isEventProvider() {
        return true;
    }

    public NetworkConnector[] getConnectors() {
        return manager.getConnectorsForContainer(this);
    }

    public NetworkConnector[] getConnectors(String protocol) {
        return manager.getConnectorsForContainer(this, protocol);
    }

    public JMSManager getManager() {
        return manager;
    }

    public void setManager(JMSManager manager) {
        this.manager = manager;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public ClassLoader getClassLoader() {
        if( classLoader == null ) {
            classLoader = this.getClass().getClassLoader();
        }
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
	
}