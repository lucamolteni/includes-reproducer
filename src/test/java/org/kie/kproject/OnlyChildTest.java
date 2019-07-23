package org.kie.kproject;

import org.drools.core.io.impl.InputStreamResource;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;

import static org.junit.Assert.*;

public class OnlyChildTest extends BaseModelTest {

    public OnlyChildTest(RUN_TYPE testRunType) {
        super(testRunType);
    }

    @Test
    public void testOnlyChild() {
        KieBase childKBase = createChildKBase();

        KieSession newChildKSession = childKBase.newKieSession();
        FactHandle stringDeleted = newChildKSession.insert("10");
        FactHandle stringInserted = newChildKSession.insert("otherstring");

        int numberOfRulesFired = newChildKSession.fireAllRules();
        assertEquals(1, numberOfRulesFired);

        assertNull(newChildKSession.getObject(stringDeleted));
        assertNotNull(newChildKSession.getObject(stringInserted));
    }

    private KieBase createChildKBase() {
        KieServices kieServices = KieServices.Factory.get();
        KieModuleModel kmodule = kieServices.newKieModuleModel();
        KieBaseModel childKbase = kmodule.newKieBaseModel(CHILD_KBASE_NAME).setDefault(true);
        childKbase.addPackage(CHILD_KBASE_PACKAGE);

        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.writeKModuleXML(kmodule.toXML());
        kieFileSystem.generateAndWritePomXML(CHILD_RELEASE_ID);

        InputStreamResource resource = new InputStreamResource(getClass().getClassLoader().getResourceAsStream("org/childkbase/childrules.drl"));
        kieFileSystem.write("src/main/resources/org/childkbase/childrules.drl", resource);
        kieServices.newKieBuilder(kieFileSystem).buildAll(buildProjectClass());
        return kieServices.newKieContainer(CHILD_RELEASE_ID).getKieBase();
    }
}
