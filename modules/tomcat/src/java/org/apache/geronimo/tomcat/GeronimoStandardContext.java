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
package org.apache.geronimo.tomcat;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Manager;
import org.apache.catalina.Valve;
import org.apache.catalina.Wrapper;
import org.apache.catalina.cluster.CatalinaCluster;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.kernel.StoredObject;
import org.apache.geronimo.naming.enc.EnterpriseNamingContext;
import org.apache.geronimo.naming.reference.ClassLoaderAwareReference;
import org.apache.geronimo.naming.reference.KernelAwareReference;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.IdentificationPrincipal;
import org.apache.geronimo.security.SubjectId;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.util.ConfigurationUtil;
import org.apache.geronimo.tomcat.interceptor.BeforeAfter;
import org.apache.geronimo.tomcat.interceptor.ComponentContextBeforeAfter;
import org.apache.geronimo.tomcat.interceptor.InstanceContextBeforeAfter;
import org.apache.geronimo.tomcat.interceptor.PolicyContextBeforeAfter;
import org.apache.geronimo.tomcat.interceptor.TransactionContextBeforeAfter;
import org.apache.geronimo.tomcat.util.SecurityHolder;
import org.apache.geronimo.tomcat.valve.DefaultSubjectValve;
import org.apache.geronimo.tomcat.valve.GeronimoBeforeAfterValve;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.webservices.POJOWebServiceServlet;
import org.apache.geronimo.webservices.WebServiceContainer;
import org.apache.geronimo.webservices.WebServiceContainerInvoker;


public class GeronimoStandardContext extends StandardContext {

    private static final Log log = LogFactory.getLog(GeronimoStandardContext.class);

    private static final long serialVersionUID = 3834587716552831032L;

    private Subject defaultSubject = null;

    private Map webServiceMap = null;

    private boolean pipelineInitialized;
    
    private BeforeAfter beforeAfter = null;
    private int contextCount = 0;
    
    public void setContextProperties(TomcatContext ctx) throws DeploymentException {
        // Create ReadOnlyContext
        javax.naming.Context enc = null;
        Map componentContext = ctx.getComponentContext();
        try {
            if (componentContext != null) {
                for (Iterator iterator = componentContext.values().iterator(); iterator.hasNext();) {
                    Object value = iterator.next();
                    if (value instanceof KernelAwareReference) {
                        ((KernelAwareReference) value).setKernel(ctx.getKernel());
                    }
                    if (value instanceof ClassLoaderAwareReference) {
                        ((ClassLoaderAwareReference) value).setClassLoader(ctx.getWebClassLoader());
                    }
                }
                enc = EnterpriseNamingContext.createEnterpriseNamingContext(componentContext);
            }
        } catch (NamingException ne) {
            log.error(ne);
        }
        
        int index = 0;
        BeforeAfter interceptor = new InstanceContextBeforeAfter(null, index++, 
                ctx.getUnshareableResources(), 
                ctx.getApplicationManagedSecurityResources(), 
                ctx.getTrackedConnectionAssociator());

        // Set ComponentContext BeforeAfter
        if (enc != null) {
            interceptor = new ComponentContextBeforeAfter(interceptor, index++, enc);
        }

        // Set TransactionContext BeforeAfter
        TransactionContextManager transactionContextManager = ctx.getTransactionContextManager();
        if (transactionContextManager != null) {
            interceptor = new TransactionContextBeforeAfter(interceptor, index++, transactionContextManager);
        }

        //Set a PolicyContext BeforeAfter
        SecurityHolder securityHolder = ctx.getSecurityHolder();
        if (securityHolder != null) {
            if (securityHolder.getPolicyContextID() != null) {

                PolicyContext.setContextID(securityHolder.getPolicyContextID());

                /**
                 * Register our default subject with the ContextManager
                 */
                DefaultPrincipal defaultPrincipal = securityHolder.getDefaultPrincipal();
                if (defaultPrincipal != null) {
                    defaultSubject = ConfigurationUtil.generateDefaultSubject(defaultPrincipal, ctx.getWebClassLoader());
                    ContextManager.registerSubject(defaultSubject);
                    SubjectId id = ContextManager.getSubjectId(defaultSubject);
                    defaultSubject.getPrincipals().add(new IdentificationPrincipal(id));
                }

                interceptor = new PolicyContextBeforeAfter(interceptor, index++, securityHolder.getPolicyContextID());
            }
        }
        
        //Set the BeforeAfters as a valve
        GeronimoBeforeAfterValve geronimoBAValve = new GeronimoBeforeAfterValve(interceptor, index);
        addValve(geronimoBAValve);
        beforeAfter = interceptor;
        contextCount = index;
        
        //Not clear if user defined valves should be involved in init processing.  Probably not since
        //request and response are null.

        addValve(new SystemMethodValve());

        // Add User Defined Valves
        List valveChain = ctx.getValveChain();
        if (valveChain != null) {
            Iterator iterator = valveChain.iterator();
            while (iterator.hasNext()) {
                Valve valve = (Valve) iterator.next();
                addValve(valve);
            }
        }

        CatalinaCluster cluster = ctx.getCluster();
        if (cluster != null)
            this.setCluster(cluster);
        
        Manager manager = ctx.getManager();
        if (manager != null)
            this.setManager(manager);

        pipelineInitialized = true;
        this.webServiceMap = ctx.getWebServices();

        this.setCrossContext(ctx.isCrossContext());
        
        //Set the Dispatch listener
        this.addInstanceListener("org.apache.geronimo.tomcat.listener.DispatchListener");
    }

    public synchronized void start() throws LifecycleException {
        if (pipelineInitialized) {
            try {
                Valve valve = getFirst();
                valve.invoke(null, null);
                //Install the DefaultSubjectValve after the authentication valve so the default subject is supplied
                //only if no real subject is authenticated.
                
                Valve defaultSubjectValve = new DefaultSubjectValve(defaultSubject);
                addValve(defaultSubjectValve);
            } catch (IOException e) {
                if (e.getCause() instanceof LifecycleException) {
                    throw (LifecycleException) e.getCause();
                }
                throw new LifecycleException(e);
            } catch (ServletException e) {
                throw new LifecycleException(e);
            }
        } else
            super.start();
    }

    public synchronized void stop() throws LifecycleException {
        // Remove the defaultSubject
        if (defaultSubject != null) {
            ContextManager.unregisterSubject(defaultSubject);
        }

        super.stop();
    }

    public void addChild(Container child) {
        Wrapper wrapper = (Wrapper) child;

        String servletClassName = wrapper.getServletClass();
        if (servletClassName == null) {
            super.addChild(child);
            return;
        }

        ClassLoader cl = this.getParentClassLoader();

        Class baseServletClass;
        Class servletClass;
        try {
            baseServletClass = cl.loadClass(Servlet.class.getName());
            servletClass = cl.loadClass(servletClassName);
            //Check if the servlet is of type Servlet class
            if (!baseServletClass.isAssignableFrom(servletClass)) {
                //Nope - its probably a webservice, so lets see...
                if (webServiceMap != null) {
                    StoredObject storedObject = (StoredObject) webServiceMap.get(wrapper.getName());

                    if (storedObject != null) {
                        WebServiceContainer webServiceContainer;
                        try {
                            webServiceContainer = (WebServiceContainer) storedObject.getObject(cl);
                        } catch (IOException io) {
                            throw new RuntimeException(io);
                        }
                        //Yep its a web service
                        //So swap it out with a POJOWebServiceServlet
                        wrapper.setServletClass("org.apache.geronimo.webservices.POJOWebServiceServlet");

                        //Set the WebServiceContainer stuff
                        String webServicecontainerID = wrapper.getName() + WebServiceContainerInvoker.WEBSERVICE_CONTAINER + webServiceContainer.hashCode();
                        getServletContext().setAttribute(webServicecontainerID, webServiceContainer);
                        wrapper.addInitParameter(WebServiceContainerInvoker.WEBSERVICE_CONTAINER, webServicecontainerID);

                        //Set the SEI Class in the attribute
                        String pojoClassID = wrapper.getName() + POJOWebServiceServlet.POJO_CLASS + servletClass.hashCode();
                        getServletContext().setAttribute(pojoClassID, servletClass);
                        wrapper.addInitParameter(POJOWebServiceServlet.POJO_CLASS, pojoClassID);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        super.addChild(child);
    }


    private class SystemMethodValve extends ValveBase {

        public void invoke(Request request, Response response) throws IOException, ServletException {
            if (request == null && response == null) {
                try {
                    GeronimoStandardContext.super.start();
                } catch (LifecycleException e) {
                    throw (IOException) new IOException("wrapping lifecycle exception").initCause(e);
                }
            } else {
                getNext().invoke(request, response);
            }

        }
    }


    public BeforeAfter getBeforeAfter() {
        return beforeAfter;
    }

    public int getContextCount() {
        return contextCount;
    }

}
