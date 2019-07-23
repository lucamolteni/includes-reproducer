package org.kie.kproject;

import org.drools.core.io.impl.InputStreamResource;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.runtime.KieSession;

import static org.junit.Assert.*;

public class ChildIncludingSuperTest extends BaseModelTest{

    public ChildIncludingSuperTest(RUN_TYPE testRunType) {
        super(testRunType);
    }

    @Test
    public void testChildIncludingSuper() {
        KieBase kBase = createKieBase();

        KieSession newSuperKieBase = kBase.newKieSession();
        newSuperKieBase.insert(10);

        int numberOfRulesFired = newSuperKieBase.fireAllRules();
        assertEquals(2, numberOfRulesFired);
        assertEquals(0, newSuperKieBase.getObjects().size());
    }

    private KieBase createKieBase() {
        KieServices kieServices = KieServices.Factory.get();
        KieModuleModel kmodule = kieServices.newKieModuleModel();

        KieBaseModel superKieBase = kmodule.newKieBaseModel(SUPER_KBASE_NAME);
        superKieBase.addPackage(SUPER_KBASE_PACKAGE);

        KieBaseModel childKbase = kmodule.newKieBaseModel(CHILD_KBASE_NAME)
                .setDefault(true)
                .addInclude(SUPER_KBASE_NAME);

        childKbase.addPackage(CHILD_KBASE_PACKAGE);

        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

        kieFileSystem.writeKModuleXML(kmodule.toXML());
        kieFileSystem.generateAndWritePomXML(SUPER_RELEASE_ID);

        InputStreamResource resource1 = new InputStreamResource(getClass().getClassLoader().getResourceAsStream("org/childkbase/childrules.drl"));
        kieFileSystem.write("src/main/resources/org/childkbase/childrules.drl", resource1);

        InputStreamResource resource2 = new InputStreamResource(getClass().getClassLoader().getResourceAsStream("org/superkbase/superrules.drl"));
        kieFileSystem.write("src/main/resources/org/superkbase/superrules.drl", resource2);

        kieServices.newKieBuilder(kieFileSystem).buildAll(buildProjectClass());


        return kieServices.newKieContainer(SUPER_RELEASE_ID).getKieBase(CHILD_KBASE_NAME);
    }
}
