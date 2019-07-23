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

public class OnlySuperTest extends BaseModelTest {

    public OnlySuperTest(RUN_TYPE testRunType) {
        super(testRunType);
    }

    @Test
    public void testOnlySuper() {
        KieBase superKieBase = createSuperKieBase();

        KieSession newSuperKieBase = superKieBase.newKieSession();
        newSuperKieBase.insert(10);

        int numberOfRulesFired = newSuperKieBase.fireAllRules();
        assertEquals(1, numberOfRulesFired);
        assertEquals(1, newSuperKieBase.getObjects().size());
    }

    private KieBase createSuperKieBase() {
        KieServices kieServices = KieServices.Factory.get();
        KieModuleModel superKModule = kieServices.newKieModuleModel();
        KieBaseModel superKieBase = superKModule.newKieBaseModel(SUPER_KBASE_NAME)
                .setDefault(true);
        superKieBase.addPackage(SUPER_KBASE_PACKAGE);

        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.writeKModuleXML(superKModule.toXML());
        kieFileSystem.generateAndWritePomXML(SUPER_RELEASE_ID);

        InputStreamResource resource = new InputStreamResource(getClass().getClassLoader().getResourceAsStream("org/superkbase/superrules.drl"));
        kieFileSystem.write("src/main/resources/org/superkbase/superrules.drl", resource);
        kieServices.newKieBuilder(kieFileSystem).buildAll(buildProjectClass());
        return kieServices.newKieContainer(SUPER_RELEASE_ID).getKieBase();
    }
}
