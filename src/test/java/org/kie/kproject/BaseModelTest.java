package org.kie.kproject;

import java.util.Arrays;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public abstract class BaseModelTest {

    public enum RUN_TYPE {
        PATTERN_DSL,
        STANDARD_FROM_DRL

    }

    final static Object[][] PLAIN = {
            new Object[]{RUN_TYPE.STANDARD_FROM_DRL},
            new Object[]{RUN_TYPE.PATTERN_DSL},
    };

    protected final RUN_TYPE testRunType;

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
}
