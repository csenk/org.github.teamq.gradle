package com.google.common.collect;

import com.google.gwt.junit.client.GWTTestCase;

public class HashMultimapTest extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "org.github.teamq.gradle.gwt.demo.Demo";
    }

    /**
     * This test method exists to enforce the compiler to recognize a guava
     * class. If guava-gwt is used some emulated classes could cause compile
     * issues in a normal java compile. If the gradle build succeeds the GWT
     * plugin did some great work :)
     */
    public void testInstantiation() {
        assertNotNull(HashMultimap.create());
    }

}
