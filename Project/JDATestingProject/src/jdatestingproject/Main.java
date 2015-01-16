/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdatestingproject;

import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 *
 * @author craz
 */
public class Main {
    public static void main(String[] args) {
        TestRunner runner = new TestRunner();
        TestSuite suite = new TestSuite();
        suite.addTest(new JDATestClass("testDeclarationAndImport"));
        suite.addTest(new JDATestClass("testNestedClass"));
        //suite.addTest(new JDATestClass("testMultipleClasses"));
        suite.addTest(new JDATestClass("testStaticAndNewClass"));
        runner.doRun(suite);
    }
}
