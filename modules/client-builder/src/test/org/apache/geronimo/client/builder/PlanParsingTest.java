package org.apache.geronimo.client.builder;

import java.io.File;

import junit.framework.TestCase;
import org.apache.geronimo.xbeans.geronimo.naming.GerLocalRefType;
import org.apache.geronimo.xbeans.geronimo.client.GerApplicationClientType;
import org.apache.geronimo.xbeans.geronimo.client.GerApplicationClientDocument;
import org.apache.geronimo.schema.SchemaConversionUtils;

/**
 */
public class PlanParsingTest extends TestCase {

    private AppClientModuleBuilder builder = new AppClientModuleBuilder(null, null,null, null, null, null);
    File basedir = new File(System.getProperty("basedir", "."));

    public void testResourceRef() throws Exception {
        File resourcePlan = new File(basedir, "src/test-resources/plans/plan1.xml");
        assertTrue(resourcePlan.exists());
        GerApplicationClientType appClient = builder.getApplicationClientType(resourcePlan, null, null, null);
        assertEquals(1, appClient.getResourceRefArray().length);
    }

    public void testConstructPlan() throws Exception {
        GerApplicationClientDocument appClientDoc = GerApplicationClientDocument.Factory.newInstance();
        GerApplicationClientType appClient = appClientDoc.addNewApplicationClient();
        appClient.setConfigId("configId");
        appClient.setParentId("parentId");
        GerLocalRefType ref = appClient.addNewResourceRef();
        ref.setRefName("ref");
        ref.setTargetName("target");

        SchemaConversionUtils.validateDD(appClient);
        System.out.println(appClient.toString());
    }
}
