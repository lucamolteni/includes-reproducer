package org.kie.kproject;

import org.appformer.maven.support.AFReleaseId;
import org.drools.core.io.impl.InputStreamResource;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.runtime.KieSession;

import static org.junit.Assert.*;

public class ChildIncludingSuperTestDifferentKJars extends BaseModelTest {

    public ChildIncludingSuperTestDifferentKJars(RUN_TYPE testRunType) {
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

        superKieBase(kieServices);
        childKieBase(kieServices);

        return kieServices.newKieContainer(CHILD_RELEASE_ID).getKieBase(CHILD_KBASE_NAME);
    }

    private void superKieBase(KieServices kieServices) {
        KieModuleModel superKModule = kieServices.newKieModuleModel();
        KieBaseModel superKieBase = superKModule.newKieBaseModel(SUPER_KBASE_NAME);

        superKieBase.addPackage(SUPER_KBASE_PACKAGE);

        KieFileSystem superFileSystem = kieServices.newKieFileSystem();

        InputStreamResource resource2 = new InputStreamResource(getClass().getClassLoader().getResourceAsStream("org/superkbase/superrules.drl"));
        superFileSystem.write("src/main/resources/org/superkbase/superrules.drl", resource2);


        superFileSystem.writeKModuleXML(superKModule.toXML());
        superFileSystem.write("pom.xml", generatePomXml(SUPER_RELEASE_ID));

        kieServices.newKieBuilder(superFileSystem).buildAll(buildProjectClass());
    }

    private void childKieBase(KieServices kieServices) {
        KieModuleModel childKModule = kieServices.newKieModuleModel();
        KieBaseModel childKbase = childKModule.newKieBaseModel(CHILD_KBASE_NAME)
                .setDefault(true)
                .addInclude(SUPER_KBASE_NAME);

        childKbase.addPackage(CHILD_KBASE_PACKAGE);

        KieFileSystem childFileSystem = kieServices.newKieFileSystem();

        InputStreamResource resource1 = new InputStreamResource(getClass().getClassLoader().getResourceAsStream("org/childkbase/childrules.drl"));
        childFileSystem.write("src/main/resources/org/childkbase/childrules.drl", resource1);

        childFileSystem.writeKModuleXML(childKModule.toXML());
        childFileSystem.write("pom.xml", generatePomXml(CHILD_RELEASE_ID, SUPER_RELEASE_ID));

        kieServices.newKieBuilder(childFileSystem).buildAll(buildProjectClass());
    }

    public static String generatePomXml(AFReleaseId releaseId, ReleaseId... dependencies) {
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n");
        sBuilder.append("         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\"> \n");
        sBuilder.append("    <modelVersion>4.0.0</modelVersion> \n");

        toGAV(releaseId, sBuilder);

        sBuilder.append("    <packaging>jar</packaging> \n");

        sBuilder.append("    <name>Default</name> \n");

        sBuilder.append("    <dependencies> \n");

        for(ReleaseId d : dependencies) {
            sBuilder.append("    <dependency> \n");
            toGAV(d, sBuilder);
            sBuilder.append("    </dependency> \n");
        }

        sBuilder.append("    </dependencies> \n");
        sBuilder.append("</project>  \n");

        return sBuilder.toString();
    }

    private static void toGAV(AFReleaseId releaseId, StringBuilder sBuilder) {
        sBuilder.append("    <groupId>");
        sBuilder.append(releaseId.getGroupId());
        sBuilder.append("</groupId> \n");

        sBuilder.append("    <artifactId>");
        sBuilder.append(releaseId.getArtifactId());
        sBuilder.append("</artifactId> \n");

        sBuilder.append("    <version>");
        sBuilder.append(releaseId.getVersion());
        sBuilder.append("</version> \n");
    }
}
