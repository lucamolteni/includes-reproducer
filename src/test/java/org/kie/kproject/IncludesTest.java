package org.kie.kproject;

import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.core.io.impl.InputStreamResource;
import org.drools.modelcompiler.ExecutableModelFlowProject;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;

import static org.junit.Assert.*;

public class IncludesTest {

    private final String CHILD_KBASE_NAME = "ChildKBase";
    private final String CHILD_KBASE_PACKAGE = "org.childkbase";
    private final ReleaseIdImpl CHILD_RELEASE_ID = new ReleaseIdImpl(CHILD_KBASE_PACKAGE, "childkbase", "1.0.0");

    private final String SUPER_KBASE_NAME = "SuperKbase";
    private final String SUPER_KBASE_PACKAGE = "org.superkbase";

    @Test
    public void testChildKBase() {
        KieBase childKBase = createChildKBase();

        KieSession newChildKSession = childKBase.newKieSession();
        FactHandle stringDeleted = newChildKSession.insert("tobedeleted");
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
        kieServices.newKieBuilder(kieFileSystem).buildAll(ExecutableModelFlowProject.class);
        return kieServices.newKieContainer(CHILD_RELEASE_ID).getKieBase();
    }

    private KieBase createSuperKieBase() {
        KieServices kieServices = KieServices.Factory.get();
        KieModuleModel superKModule = kieServices.newKieModuleModel();
        KieBaseModel superKieBase = superKModule.newKieBaseModel(SUPER_KBASE_NAME).addInclude(CHILD_KBASE_NAME);
        superKieBase.addPackage(SUPER_KBASE_PACKAGE);
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.writeKModuleXML(superKModule.toXML());
        kieServices.newKieBuilder(kieFileSystem).buildAll(ExecutableModelFlowProject.class);
        return kieServices.newKieContainer(CHILD_RELEASE_ID).getKieBase();
    }
}
