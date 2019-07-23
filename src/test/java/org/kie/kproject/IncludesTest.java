package org.kie.kproject;

import org.drools.compiler.kie.builder.impl.DrlProject;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.core.io.impl.InputStreamResource;
import org.drools.modelcompiler.ExecutableModelFlowProject;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;

import static org.junit.Assert.*;

public class IncludesTest  extends BaseModelTest{

    private final String CHILD_KBASE_NAME = "ChildKBase";
    private final String CHILD_KBASE_PACKAGE = "org.childkbase";
    private final ReleaseIdImpl CHILD_RELEASE_ID = new ReleaseIdImpl(CHILD_KBASE_PACKAGE, "childkbase", "1.0.0");

    private final String SUPER_KBASE_NAME = "SuperKbase";
    private final String SUPER_KBASE_PACKAGE = "org.superkbase";
    private final ReleaseIdImpl SUPER_RELEASE_ID = new ReleaseIdImpl(SUPER_KBASE_PACKAGE, "superkbase", "1.0.0");

    public IncludesTest(RUN_TYPE testRunType) {
        super(testRunType);
    }

    @Test
    public void testChildKBase() {
        KieBase childKBase = createChildKBase();

        KieSession newChildKSession = childKBase.newKieSession();
        FactHandle stringDeleted = newChildKSession.insert("10");
        FactHandle stringInserted = newChildKSession.insert("otherstring");

        int numberOfRulesFired = newChildKSession.fireAllRules();
        assertEquals(1, numberOfRulesFired);

        assertNull(newChildKSession.getObject(stringDeleted));
        assertNotNull(newChildKSession.getObject(stringInserted));

    }

    @Test
    public void testSuperKieBase() {
        KieBase superKieBase = createSuperKieBase();

        KieSession newSuperKieBase = superKieBase.newKieSession();
        newSuperKieBase.insert(10);

        int numberOfRulesFired = newSuperKieBase.fireAllRules();
        assertEquals(1, numberOfRulesFired);
        assertEquals(1, newSuperKieBase.getObjects().size());
    }

    @Test
    public void testInclude() {
        KieBase kBase = thirdCase();

        KieSession newSuperKieBase = kBase.newKieSession();
        newSuperKieBase.insert(10);

        int numberOfRulesFired = newSuperKieBase.fireAllRules();
        assertEquals(2, numberOfRulesFired);
        assertEquals(0, newSuperKieBase.getObjects().size());
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

    private KieBase thirdCase() {
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

    private Class<? extends KieBuilder.ProjectType> buildProjectClass() {
        return isExecutableModel() ? ExecutableModelFlowProject.class : DrlProject.class;
    }
}
