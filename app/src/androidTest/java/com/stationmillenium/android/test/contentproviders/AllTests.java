package com.stationmillenium.android.test.contentproviders;

import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * {@link TestSuite} for all tests
 *
 * @author vincent
 */
public class AllTests extends TestSuite {

    /**
     * Build the {@link TestSuite}
     *
     * @return the {@link Test} as {@link TestSuite}
     */
    public static Test suite() {
        return new TestSuiteBuilder(AllTests.class).includeAllPackagesUnderHere().build();
    }

}
