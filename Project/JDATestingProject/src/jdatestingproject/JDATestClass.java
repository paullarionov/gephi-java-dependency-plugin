/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdatestingproject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.dependenciesanalyzer.codeanalyzer.CodeAnalyzerController;

/**
 *
 * @author craz
 */
public class JDATestClass extends TestCase {
    static Logger log;
    JDATestClass (String testName) {
        super(testName);
        /*try {
            FileHandler hand = new FileHandler("JDATestLog.xml");
            log = Logger.getLogger("JDATestLog");
            log.addHandler(hand);
        } catch (IOException e) {
            System.out.println("Log init failed.");
            e.printStackTrace();
        }*/
    }
    
    public void testDeclarationAndImport() throws IOException {
        Map<String, Map> dependencies = new HashMap<String, Map>();
        Map<String, Map> dependencies_model = new HashMap<String, Map>();
        Map<String, String> dependencies_components= new HashMap<String, String>();
        CodeAnalyzerController controller = new CodeAnalyzerController(Logger.global);
        
        System.out.println(System.getProperty("user.dir"));
        controller.invokeProcessor(System.getProperty("user.dir") + "/src/jdatestingproject/testFiles/ClassDeclaration.java", false); //Передача пути к файлам для анализа
        dependencies = controller.getResults();
        dependencies_components.put("MacOutputStream", "org.jcp.xml.dsig.internal.MacOutputStream");
        dependencies_model.put("jdatestingproject.testFiles.ClassDeclaration", dependencies_components);
        assertEquals(dependencies_model, dependencies);
        System.out.println("Ok");
    }
    
    public void testNestedClass() throws IOException {
        Map<String, Map> dependencies = new HashMap<String, Map>();
        Map<String, Map> dependencies_model = new HashMap<String, Map>();
        Map<String, String> dependencies_components= new HashMap<String, String>();
        CodeAnalyzerController controller = new CodeAnalyzerController(Logger.global);
        
        System.out.println(System.getProperty("user.dir"));
        controller.invokeProcessor(System.getProperty("user.dir") + "/src/jdatestingproject/testFiles/NestedClassSample.java", false); //Передача пути к файлам для анализа
        dependencies = controller.getResults();
        
        dependencies_components.put("NestedClassPrivate", "jdatestingproject.testFiles.NestedClassSample.NestedClassPrivate");
        dependencies_components.put("NestedClassProtected", "jdatestingproject.testFiles.NestedClassSample.NestedClassProtected");
        dependencies_components.put("NestedClassPublic", "jdatestingproject.testFiles.NestedClassSample.NestedClassPublic");
        dependencies_components.put("NestedClass", "jdatestingproject.testFiles.NestedClassSample.NestedClass");
        dependencies_model.put("jdatestingproject.testFiles.NestedClassSample", dependencies_components);
        assertEquals(dependencies_model, dependencies);
        System.out.println("Ok");
    }
    public void testMultipleClasses() throws IOException {
        Map<String, Map> dependencies = new HashMap<String, Map>();
        Map<String, Map> dependencies_model = new HashMap<String, Map>();
        Map<String, String> dependencies_components= new HashMap<String, String>();
        CodeAnalyzerController controller = new CodeAnalyzerController(Logger.global);
        System.out.println(System.getProperty("user.dir"));
        controller.invokeProcessor(System.getProperty("user.dir") + "/src/jdatestingproject/testFiles/MultipleClassesSample.java", false); //Передача пути к файлам для анализа
        dependencies = controller.getResults();
        
        dependencies_components.put("ProtectedClass", "jdatestingproject.testFiles.MultipleClassesSample.ProtectedClass");
        dependencies_components.put("PublicClass", "jdatestingproject.testFiles.MultipleClassesSample.PublicClass");
        dependencies_components.put("MyClass", "jdatestingproject.testFiles.MultipleClassesSample.MyClass");
        dependencies_model.put("jdatestingproject.testFiles.MultipleClassesSample", dependencies_components);
        assertEquals(dependencies_model, dependencies);
        System.out.println("Ok");
    }
    
    public void testStaticAndNewClass() throws IOException {
        Map<String, Map> dependencies = new HashMap<String, Map>();
        Map<String, Map> dependencies_model = new HashMap<String, Map>();
        Map<String, String> dependencies_components= new HashMap<String, String>();
        CodeAnalyzerController controller = new CodeAnalyzerController(Logger.global);
        
        System.out.println(System.getProperty("user.dir"));
        controller.invokeProcessor(System.getProperty("user.dir") + "/src/jdatestingproject/testFiles", true); //Передача пути к файлам для анализа
        dependencies = controller.getResults();
        
        dependencies_components.put("ClassDeclaration", "jdatestingproject.testFiles.ClassDeclaration");
        dependencies_components.put("MultipleClassesSample", "jdatestingproject.testFiles.MultipleClassesSample");
        dependencies_model.put("jdatestingproject.testFiles.StaticAndNewClassSample", dependencies_components);
        assertEquals(dependencies_components, dependencies.get("jdatestingproject.testFiles.StaticAndNewClassSample"));
        System.out.println("Ok");
    }
}
