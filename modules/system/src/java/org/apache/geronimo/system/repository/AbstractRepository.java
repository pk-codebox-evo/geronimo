/**
 *
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
package org.apache.geronimo.system.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactTypeHandler;
import org.apache.geronimo.kernel.repository.FileWriteMonitor;
import org.apache.geronimo.kernel.repository.WriteableRepository;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractRepository implements WriteableRepository {
    protected static final Log log = LogFactory.getLog(AbstractRepository.class);
    private final static ArtifactTypeHandler DEFAULT_TYPE_HANDLER = new CopyArtifactTypeHandler();
    protected final File rootFile;
    private final Map typeHandlers = new HashMap();

    public AbstractRepository(URI root, ServerInfo serverInfo) {
        this(resolveRoot(root, serverInfo));
    }

    public AbstractRepository(File rootFile) {
        if (rootFile == null) throw new NullPointerException("root is null");

        if (!rootFile.exists() || !rootFile.isDirectory() || !rootFile.canRead()) {
            throw new IllegalStateException("Maven2Repository must have a root that's a valid readable directory (not " + rootFile.getAbsolutePath() + ")");
        }

        this.rootFile = rootFile;
        log.debug("Repository root is " + rootFile.getAbsolutePath());

        typeHandlers.put("car", new UnpackArtifactTypeHandler());
    }

    private static File resolveRoot(URI root, ServerInfo serverInfo) {
        if (root == null) throw new NullPointerException("root is null");

        if (!root.toString().endsWith("/")) {
            try {
                root = new URI(root.toString() + "/");
            } catch (URISyntaxException e) {
                throw new RuntimeException("Invalid repository root (does not end with / ) and can't add myself", e);
            }
        }

        URI resolvedUri;
        if (serverInfo != null) {
            resolvedUri = serverInfo.resolve(root);
        } else {
            resolvedUri = root;
        }

        if (!resolvedUri.getScheme().equals("file")) {
            throw new IllegalStateException("FileSystemRepository must have a root that's a local directory (not " + resolvedUri + ")");
        }

        File rootFile = new File(resolvedUri);
        return rootFile;
    }

    public boolean contains(Artifact artifact) {
        if(!artifact.isResolved()) {
            throw new IllegalArgumentException("Artifact "+artifact+" is not fully resolved");
        }
        File location = getLocation(artifact);
        return location.canRead() && (location.isFile() || new File(location, "META-INF").isDirectory());
    }

    private static final String NAMESPACE = "http://geronimo.apache.org/xml/ns/deployment-1.1";
    public LinkedHashSet getDependencies(Artifact artifact) {
        if(!artifact.isResolved()) {
            throw new IllegalArgumentException("Artifact "+artifact+" is not fully resolved");
        }
        LinkedHashSet dependencies = new LinkedHashSet();
        URL url;
        try {
            File location = getLocation(artifact);
            url = location.toURL();
        } catch (MalformedURLException e) {
            throw (IllegalStateException)new IllegalStateException("Unable to get URL for dependency " + artifact).initCause(e);
        }
        ClassLoader depCL = new URLClassLoader(new URL[]{url}, ClassLoader.getSystemClassLoader());
        InputStream is = depCL.getResourceAsStream("META-INF/geronimo-dependency.xml");
        if (is != null) {
            InputSource in = new InputSource(is);
            DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
            dfactory.setNamespaceAware(true);
            try {
                Document doc = dfactory.newDocumentBuilder().parse(in);
                Element root = doc.getDocumentElement();
                NodeList configs = root.getElementsByTagNameNS(NAMESPACE, "dependency");
                for (int i = 0; i < configs.getLength(); i++) {
                    Element dependencyElement = (Element) configs.item(i);
                    String groupId = getString(dependencyElement, "groupId");
                    String artifactId = getString(dependencyElement, "artifactId");
                    String version = getString(dependencyElement, "version");
                    String type = getString(dependencyElement, "type");
                    if (type == null) {
                        type = "jar";
                    }
                    dependencies.add(new Artifact(groupId, artifactId,  version, type));
                }
            } catch (IOException e) {
                throw (IllegalStateException)new IllegalStateException("Unable to parse geronimo-dependency.xml file in " + url).initCause(e);
            } catch (ParserConfigurationException e) {
                throw (IllegalStateException)new IllegalStateException("Unable to parse geronimo-dependency.xml file in " + url).initCause(e);
            } catch (SAXException e) {
                throw (IllegalStateException)new IllegalStateException("Unable to parse geronimo-dependency.xml file in " + url).initCause(e);
            }
        }
        return dependencies;
    }

    private String getString(Element dependencyElement, String childName) {
        NodeList children = dependencyElement.getElementsByTagNameNS(NAMESPACE, childName);
        if (children == null || children.getLength() == 0) {
        return null;
        }
        String value = "";
        NodeList text = children.item(0).getChildNodes();
        for (int t = 0; t < text.getLength(); t++) {
            Node n = text.item(t);
            if (n.getNodeType() == Node.TEXT_NODE) {
                value += n.getNodeValue();
            }
        }
        return value.trim();
    }

    public void setTypeHandler(String type, ArtifactTypeHandler handler) {
        typeHandlers.put(type, handler);
    }

    public void copyToRepository(File source, Artifact destination, FileWriteMonitor monitor) throws IOException {
        if(!destination.isResolved()) {
            throw new IllegalArgumentException("Artifact "+destination+" is not fully resolved");
        }
        if (!source.exists() || !source.canRead() || source.isDirectory()) {
            throw new IllegalArgumentException("Cannot read source file at " + source.getAbsolutePath());
        }
        copyToRepository(new FileInputStream(source), (int)source.length(), destination, monitor);
    }

    public void copyToRepository(InputStream source, int size, Artifact destination, FileWriteMonitor monitor) throws IOException {
        if(!destination.isResolved()) {
            throw new IllegalArgumentException("Artifact "+destination+" is not fully resolved");
        }
        // is this a writable repository
        if (!rootFile.canWrite()) {
            throw new IllegalStateException("This repository is not writable: " + rootFile.getAbsolutePath() + ")");
        }

        // where are we going to install the file
        File location = getLocation(destination);

        // assure that there isn't already a file installed at the specified location
        if (location.exists()) {
            throw new IllegalArgumentException("Destination " + location.getAbsolutePath() + " already exists!");
        }

        ArtifactTypeHandler typeHandler = (ArtifactTypeHandler) typeHandlers.get(destination.getType());
        if (typeHandler == null) typeHandler = DEFAULT_TYPE_HANDLER;
        typeHandler.install(source, size, destination, monitor, location);

        if (destination.getType().equalsIgnoreCase("car")) {
            System.out.println("############################################################");
            System.out.println("# Installed configuration");
            System.out.println("#   id = " + destination);
            System.out.println("#   location = " + location);
            System.out.println("############################################################");
        }
    }
}
