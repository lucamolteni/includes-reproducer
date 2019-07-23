package org.kie.kproject;

import java.util.Arrays;

import org.drools.compiler.kie.builder.impl.DrlProject;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.modelcompiler.ExecutableModelFlowProject;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.builder.KieBuilder;

@RunWith(Parameterized.class)
public abstract class BaseModelTest {

    public enum RUN_TYPE {
        PATTERN_DSL,
        STANDARD_FROM_DRL

    }

    static final Object[][] PLAIN = {
            new Object[]{RUN_TYPE.STANDARD_FROM_DRL},
            new Object[]{RUN_TYPE.PATTERN_DSL},
    };

    final RUN_TYPE testRunType;

    public BaseModelTest(RUN_TYPE testRunType) {
        this.testRunType = testRunType;
    }

    public boolean isExecutableModel() {
        return testRunType.equals(RUN_TYPE.PATTERN_DSL);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> params() {
        return Arrays.asList(PLAIN);
    }

    protected Class<? extends KieBuilder.ProjectType> buildProjectClass() {
        return isExecutableModel() ? ExecutableModelFlowProject.class : DrlProject.class;
    }

    protected final String CHILD_KBASE_NAME = "ChildKBase";
    protected final String CHILD_KBASE_PACKAGE = "org.childkbase";
    protected final ReleaseIdImpl CHILD_RELEASE_ID = new ReleaseIdImpl(CHILD_KBASE_PACKAGE, "childkbase", "1.0.0");

    protected final String SUPER_KBASE_NAME = "SuperKbase";
    protected final String SUPER_KBASE_PACKAGE = "org.superkbase";
    protected final ReleaseIdImpl SUPER_RELEASE_ID = new ReleaseIdImpl(SUPER_KBASE_PACKAGE, "superkbase", "1.0.0");

}
