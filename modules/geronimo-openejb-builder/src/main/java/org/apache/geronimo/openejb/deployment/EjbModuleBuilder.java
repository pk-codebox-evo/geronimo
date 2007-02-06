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
package org.apache.geronimo.openejb.deployment;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.jar.JarFile;

import javax.ejb.SessionContext;
import javax.ejb.EntityContext;
import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilderCollection;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.service.GBeanBuilder;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.ResourceEnvironmentSetter;
import org.apache.geronimo.openejb.EjbDeployment;
import org.apache.geronimo.openejb.EjbModuleImplGBean;
import org.apache.geronimo.openejb.OpenEjbSystem;
import org.apache.geronimo.openejb.xbeans.ejbjar.OpenejbGeronimoEjbJarType;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerSecurityDocument;
import org.apache.geronimo.xbeans.javaee.AssemblyDescriptorType;
import org.apache.geronimo.xbeans.javaee.EjbJarType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.config.ReadDescriptors;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.CmpJarBuilder;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.MessageDestinationRef;
import org.apache.openejb.jee.PersistenceContextRef;
import org.apache.openejb.jee.PersistenceUnitRef;
import org.apache.openejb.jee.ResourceEnvRef;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.ServiceRef;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.oejb2.GeronimoEjbJarType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlCursor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Master builder for processing EJB JAR deployments and creating the
 * correspinding runtime objects (GBeans, etc.).
 *
 * @version $Revision: 479481 $ $Date: 2006-11-26 16:52:20 -0800 (Sun, 26 Nov 2006) $
 */
public class EjbModuleBuilder implements ModuleBuilder {
    private static final Log log = LogFactory.getLog(EjbModuleBuilder.class);
    private static final String OPENEJBJAR_NAMESPACE = XmlUtil.OPENEJBJAR_QNAME.getNamespaceURI();

    private final Environment defaultEnvironment;
    private final Collection webServiceBuilders;
    private final NamespaceDrivenBuilderCollection securityBuilders;
    private final NamespaceDrivenBuilderCollection serviceBuilders;
    private final NamingBuilder namingBuilder;
    private final ResourceEnvironmentSetter resourceEnvironmentSetter;
    private final OpenEjbSystem openEjbSystem;

    public EjbModuleBuilder(Environment defaultEnvironment,
                            OpenEjbSystem openEjbSystem,
                            Collection webServiceBuilder,
                            Collection securityBuilders,
                            Collection serviceBuilders,
                            NamingBuilder namingBuilders,
                            ResourceEnvironmentSetter resourceEnvironmentSetter) {

        this.openEjbSystem = openEjbSystem;
        this.defaultEnvironment = defaultEnvironment;
        this.webServiceBuilders = webServiceBuilder;
        this.securityBuilders = new NamespaceDrivenBuilderCollection(securityBuilders, GerSecurityDocument.type.getDocumentElementName());
        this.serviceBuilders = new NamespaceDrivenBuilderCollection(serviceBuilders, GBeanBuilder.SERVICE_QNAME);
        this.namingBuilder = namingBuilders;
        this.resourceEnvironmentSetter = resourceEnvironmentSetter;
    }

    public String getSchemaNamespace() {
        return EjbModuleBuilder.OPENEJBJAR_NAMESPACE;
    }

    public Module createModule(File plan, JarFile moduleFile, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        return createModule(plan, moduleFile, "ejb", null, null, null, naming, idBuilder);
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        return createModule(plan, moduleFile, targetPath, specDDUrl, environment, earName, naming, idBuilder);
    }

    private Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment earEnvironment, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        if (moduleFile == null) {
            throw new NullPointerException("moduleFile is null");
        }
        if (targetPath == null) {
            throw new NullPointerException("targetPath is null");
        }
        if (targetPath.endsWith("/")) {
            throw new IllegalArgumentException("targetPath must not end with a '/'");
        }


        DeploymentLoader loader = new DeploymentLoader();
        File file = new File(moduleFile.getName());

        AppModule appModule = null;
        try {
            appModule = loader.load(file);
        } catch (UnsupportedOperationException e){
            // not an ejb module
        } catch (OpenEJBException e) {
            throw new DeploymentException(e);
        }

        if (appModule.getEjbModules().size() == 0){
            // not an ejb module
            return null;
        }
        
        org.apache.openejb.config.EjbModule ejbModule = appModule.getEjbModules().get(0);


        XmlObject unknownXmlObject = null;

        if (plan instanceof XmlObject) {
            unknownXmlObject = (XmlObject) plan;
        } else if (plan != null) {
            try {
                unknownXmlObject = XmlBeansUtil.parse(((File) plan).toURL(), XmlUtil.class.getClassLoader());
            } catch (Exception e) {
                throw new DeploymentException(e);
            }
        }

        if (unknownXmlObject != null) {
            XmlCursor xmlCursor = unknownXmlObject.newCursor();
            xmlCursor.toFirstChild();
            QName qname = xmlCursor.getName();
            if (qname.getLocalPart().equals("openejb-jar")){
                ejbModule.getAltDDs().put("openejb-jar.xml", unknownXmlObject.xmlText());
            } else if (qname.getLocalPart().equals("ejb-jar") && qname.getNamespaceURI().equals("http://geronimo.apache.org/xml/ns/j2ee/ejb/openejb-2.0")){
                ejbModule.getAltDDs().put("geronimo-openejb.xml", unknownXmlObject.xmlText());
            }
        }

        if (specDDUrl != null) {
            ejbModule.getAltDDs().put("ejb-jar.xml", specDDUrl);
        }

        ReadDescriptors readDescriptors = new ReadDescriptors();

        try {
            readDescriptors.deploy(appModule);
        } catch (OpenEJBException e) {
            throw new DeploymentException("Failed parsing descriptors for module: "+moduleFile.getName(), e);
        }

        EjbJar ejbJar = ejbModule.getEjbJar();
        OpenejbJar openejbJar = ejbModule.getOpenejbJar();

        OpenejbGeronimoEjbJarType geronimoOpenejb;


        boolean standAlone = earEnvironment == null;

        GeronimoEjbJarType jaxbGeronimoOpenejb = (GeronimoEjbJarType) ejbModule.getAltDDs().get("geronimo-openejb.xml");
        if (jaxbGeronimoOpenejb != null) {
            geronimoOpenejb = XmlUtil.convertToXmlbeans(jaxbGeronimoOpenejb);
        } else {
            String path = (standAlone)? new File(moduleFile.getName()).getName(): targetPath;
            geronimoOpenejb = XmlUtil.createDefaultPlan(path, ejbJar);
        }

        // initialize the geronimo environment
        EnvironmentType environmentType = geronimoOpenejb.getEnvironment();
        Environment environment = EnvironmentBuilder.buildEnvironment(environmentType, defaultEnvironment);
        if (earEnvironment != null) {
            EnvironmentBuilder.mergeEnvironments(earEnvironment, environment);
            environment = earEnvironment;
            if (!environment.getConfigId().isResolved()) {
                throw new IllegalStateException("EJB module ID should be fully resolved (not " + environment.getConfigId() + ")");
            }
        } else {
            idBuilder.resolve(environment, new File(moduleFile.getName()).getName(), "jar");
        }

        // todo THIS WILL NOT WORK WITH ANNOTATIONS... move this to initContext when naming is fixed
        // since assembly descriptor will only be valid once metadata complete
        // which is only available once a class loader has been constructed in the init phase
        EjbJarType ejbJarType = XmlUtil.convertToXmlbeans(ejbJar);
        if (ejbJar.getAssemblyDescriptor() != null) {
            AssemblyDescriptorType assemblyDescriptor = ejbJarType.getAssemblyDescriptor();
            namingBuilder.buildEnvironment(assemblyDescriptor, geronimoOpenejb, environment);
        }

        //overridden web service locations
        Map correctedPortLocations = new HashMap();

        // todo
//        OpenejbSessionBeanType[] openejbSessionBeans = openejbJar.getEnterpriseBeans().getSessionArray();
//        for (int i = 0; i < openejbSessionBeans.length; i++) {
//            OpenejbSessionBeanType sessionBean = openejbSessionBeans[i];
//                if (sessionBean.isSetWebServiceAddress()) {
//                    String location = sessionBean.getWebServiceAddress().trim();
//                    correctedPortLocations.put(sessionBean.getEjbName(), location);
//                }
//        }
        Map sharedContext = new HashMap();
        for (Iterator iterator = webServiceBuilders.iterator(); iterator.hasNext();) {
            WebServiceBuilder serviceBuilder = (WebServiceBuilder) iterator.next();
            serviceBuilder.findWebServices(moduleFile, true, correctedPortLocations, environment, sharedContext);
        }

        AbstractName moduleName;
        if (earName == null) {
            earName = naming.createRootName(environment.getConfigId(), NameFactory.NULL, NameFactory.J2EE_APPLICATION);
            moduleName = naming.createChildName(earName, environment.getConfigId().toString(), NameFactory.EJB_MODULE);
        } else {
            moduleName = naming.createChildName(earName, targetPath, NameFactory.EJB_MODULE);
        }

        return new EjbModule(standAlone, moduleName, environment, moduleFile, targetPath, ejbJar, openejbJar, geronimoOpenejb, "", sharedContext);
    }

    protected static void unmapReferences(EjbJar ejbJar) {
        for (EnterpriseBean enterpriseBean : ejbJar.getEnterpriseBeans()) {
            enterpriseBean.getEnvEntry().clear();
            enterpriseBean.getEjbRef().clear();
            enterpriseBean.getEjbLocalRef().clear();

            for (MessageDestinationRef ref : enterpriseBean.getMessageDestinationRef()) {
                ref.setMappedName(null);
                ref.getInjectionTarget().clear();
            }
            for (PersistenceContextRef ref : enterpriseBean.getPersistenceContextRef()) {
                ref.setMappedName(null);
                ref.getInjectionTarget().clear();
            }
            for (PersistenceUnitRef ref : enterpriseBean.getPersistenceUnitRef()) {
                ref.setMappedName(null);
                ref.getInjectionTarget().clear();
            }
            for (ResourceRef ref : enterpriseBean.getResourceRef()) {
                ref.setMappedName(null);
                ref.getInjectionTarget().clear();
            }
            for (Iterator<ResourceEnvRef> iterator = enterpriseBean.getResourceEnvRef().iterator(); iterator.hasNext();) {
                ResourceEnvRef ref = iterator.next();
                if (ref.getType().equals(SessionContext.class.getName())) {
                    iterator.remove();
                } else if (ref.getType().equals(EntityContext.class.getName())) {
                    iterator.remove();
                } else {
                    ref.setMappedName(null);
                }
                ref.getInjectionTarget().clear();

            }
            for (ServiceRef ref : enterpriseBean.getServiceRef()) {
                ref.setMappedName(null);
                ref.getInjectionTarget().clear();
            }
        }
    }


    public void installModule(JarFile earFile, EARContext earContext, Module module, Collection configurationStores, ConfigurationStore targetConfigurationStore, Collection repository) throws DeploymentException {
        installModule(module, earContext);
    }

    private void installModule(Module module, EARContext earContext) throws DeploymentException {
        JarFile moduleFile = module.getModuleFile();
        try {
            // extract the ejbJar file into a standalone packed jar file and add the contents to the output
            earContext.addIncludeAsPackedJar(URI.create(module.getTargetPath()), moduleFile);
        } catch (IOException e) {
            throw new DeploymentException("Unable to copy ejb module jar into configuration: " + moduleFile.getName());
        }
    }

    public void initContext(EARContext earContext, Module module, ClassLoader classLoader) throws DeploymentException {
        EjbModule ejbModule = (EjbModule) module;
        ejbModule.setClassLoader(classLoader);
        ejbModule.setEarContext(earContext);
        ejbModule.setRootEarContext(earContext);

        // build the config info tree
        // this method fills in the ejbJar jaxb tree based on the annotations
        // (metadata complete) and it run the openejb verifier
        EjbJarInfo ejbJarInfo;
        try {
            ejbJarInfo = openEjbSystem.configureApplication(ejbModule.getEjbModule());
            ejbModule.setEjbJarInfo(ejbJarInfo);
        } catch (OpenEJBException e) {
            e.printStackTrace();
            throw new DeploymentException(e);
        }
        EarData earData = (EarData) earContext.getGeneralData().get(EarData.class);
        if (earData == null) {
            earData = new EarData();
            earContext.getGeneralData().put(EarData.class, earData);
        }
        earData.getEjbJars().add(ejbJarInfo);

        // generate the CMP2 implementation classes
        // Generate the cmp2 concrete subclasses
        AppInfo appInfo = new AppInfo();
        appInfo.ejbJars.add(ejbJarInfo);
        CmpJarBuilder cmp2Builder = new CmpJarBuilder(appInfo, classLoader);
        try {
            File generatedJar = cmp2Builder.getJarFile();
            if (generatedJar != null) {
                String generatedPath = module.getTargetPath();
                if (generatedPath.endsWith(".jar")) {
                    generatedPath = generatedPath.substring(0, generatedPath.length() -4);
                }
                generatedPath += "-cmp2.jar";
                earContext.addInclude(URI.create(generatedPath), generatedJar);
            }
        } catch (IOException e) {
            throw new DeploymentException(e);
        }

        // update the original spec dd with the metadata complete dd
        EjbJar ejbJar = ejbModule.getEjbJar();
        ejbModule.setOriginalSpecDD(XmlUtil.marshal(ejbModule.getEjbJar()));

        // We must set all mapped name references back to null or Geronimo will blow up
        unmapReferences(ejbJar);

        // create a xmlbeans version of the ejb-jar.xml file, because the jndi code is coupled based on xmlbeans objects
        EjbJarType ejbJarType = XmlUtil.convertToXmlbeans(ejbJar);
        ejbModule.setSpecDD(ejbJarType);


        // todo move namingBuilders.buildEnvironment() here when geronimo naming supports it

        // initialize the naming builders
        OpenejbGeronimoEjbJarType geronimoOpenejb = (OpenejbGeronimoEjbJarType) ejbModule.getVendorDD();
        if (ejbJarType.getAssemblyDescriptor() != null) {
            namingBuilder.initContext(ejbJarType.getAssemblyDescriptor(),
                    geronimoOpenejb,
                    ejbModule.getEarContext().getConfiguration(),
                    earContext.getConfiguration(),
                    ejbModule);
        }

        EjbDeploymentBuilder ejbDeploymentBuilder = new EjbDeploymentBuilder(earContext, ejbModule, namingBuilder, resourceEnvironmentSetter);
        ejbModule.setEjbBuilder(ejbDeploymentBuilder);
        ejbDeploymentBuilder.initContext();

        // Build the security configuration.  Attempt to auto generate role mappings.
        securityBuilders.build(geronimoOpenejb, earContext, ejbModule.isStandAlone() ? ejbModule.getEarContext() : null);

        // Add extra gbean declared in the geronimo-openejb.xml file
        serviceBuilders.build(geronimoOpenejb, earContext, ejbModule.getEarContext());
    }

    /**
     * Does the meaty work of processing the deployment information and
     * creating GBeans for all the EJBs in the JAR, etc.
     */
    public void addGBeans(EARContext earContext, Module module, ClassLoader cl, Collection repositories) throws DeploymentException {
        EjbModule ejbModule = (EjbModule) module;
        EjbDeploymentBuilder ejbDeploymentBuilder = ejbModule.getEjbBuilder();

        // Add JSR77 EJBModule GBean
        GBeanData ejbModuleGBeanData = new GBeanData(ejbModule.getModuleName(), EjbModuleImplGBean.GBEAN_INFO);
        try {
            ejbModuleGBeanData.setReferencePattern("J2EEServer", earContext.getServerName());
            if (!ejbModule.isStandAlone()) {
                ejbModuleGBeanData.setReferencePattern("J2EEApplication", earContext.getModuleName());
            }

            ejbModuleGBeanData.setAttribute("deploymentDescriptor", ejbModule.getOriginalSpecDD());

            ejbModuleGBeanData.setReferencePatterns("EJBCollection",
                    new ReferencePatterns(new AbstractNameQuery(null,
                            Collections.singletonMap(NameFactory.EJB_MODULE, ejbModule.getModuleName().getNameProperty(NameFactory.J2EE_NAME)),
                            EjbDeployment.class.getName())));

            ejbModuleGBeanData.setReferencePattern("OpenEjbSystem", new AbstractNameQuery(null, Collections.EMPTY_MAP, OpenEjbSystem.class.getName()));
            ejbModuleGBeanData.setAttribute("ejbJarInfo", ejbModule.getEjbJarInfo());

            earContext.addGBean(ejbModuleGBeanData);
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize EJBModule GBean " + ejbModuleGBeanData.getAbstractName(), e);
        }

        // add a depdendency on the ejb module object
        ejbDeploymentBuilder.addEjbModuleDependency(ejbModuleGBeanData.getAbstractName());

        // add enc
        ejbDeploymentBuilder.buildEnc();

        // add the Jacc permissions to the ear
        ComponentPermissions componentPermissions = ejbDeploymentBuilder.buildComponentPermissions();
        earContext.addSecurityContext(ejbModule.getEjbJarInfo().moduleId, componentPermissions);
    }

    public static class EarData {
        private final Collection<EjbJarInfo> ejbJars = new ArrayList<EjbJarInfo>();

        public Collection<EjbJarInfo> getEjbJars() {
            return ejbJars;
        }
    }


    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(EjbModuleBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true);
        infoBuilder.addReference("OpenEjbSystem", OpenEjbSystem.class);
        infoBuilder.addReference("WebServiceBuilder", WebServiceBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("SecurityBuilders", NamespaceDrivenBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("ServiceBuilders", NamespaceDrivenBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("NamingBuilders", NamingBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("ResourceEnvironmentSetter", ResourceEnvironmentSetter.class, NameFactory.MODULE_BUILDER);

        infoBuilder.setConstructor(new String[]{
                "defaultEnvironment",
                "OpenEjbSystem",
                "WebServiceBuilder",
                "SecurityBuilders",
                "ServiceBuilders",
                "NamingBuilders",
                "ResourceEnvironmentSetter"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
