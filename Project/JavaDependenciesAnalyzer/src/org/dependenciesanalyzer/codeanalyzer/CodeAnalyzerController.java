/*
 * Authors :   Evgeniy Shutov
 *             Paul Larionov
 *             
 * Website :   https://bitbucket.org/paullarionov/gephi
 * 
 *
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"
 */

package org.dependenciesanalyzer.codeanalyzer;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Trees;
import java.io.*;
import java.lang.annotation.AnnotationTypeMismatchException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;
import org.dependenciesanalyzer.dependenciesinfo.JavaDependenciesInfo;

/**
 * The controller class to get the AST and initiate its parsing by our methods.
 */
public class CodeAnalyzerController {

    /**
     * Invokes the annotation processor passing the list of file names
     *
     * @param fileNames Names of files to be verified
     */
    private Map<String, Map> results = new HashMap<String, Map>();
    private List<String> errors = new LinkedList<String>();
    private int countTotal = 0;
    private int countCurrent = 0;
    private int countErrors = 0;
    String errorList;
    Logger log;
    
    String eol = System.getProperty("line.separator");
    String fs = System.getProperty("file.separator");
    
    public CodeAnalyzerController(Logger log, String errorList) {
        this.log = log;
        this.errorList = errorList;
    }

    public void invokeProcessor(List<File> files) throws IOException
    {
        // Gets the Java programming language compiler instance
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        // Gets a new instance of the standard file manager implementation
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        if (files.size() > 0) {
            for (File f : files) {
                
                //log.info("\n" + f + "\n");

                // Gets the list of java file objects
                Iterable<? extends JavaFileObject> compilationUnits1 = fileManager.getJavaFileObjects(f);
                // Creates an instance of TreeVisitor
                CodeAnalyzerTreeVisitor visitor = new CodeAnalyzerTreeVisitor();
                visitor.setLogger(log);
                visitor.setFileList(files);
                
                Iterable<? extends CompilationUnitTree> parseResult = null;
                DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
                // Creates the task and get CompilatioUnitTree objects for every file
                CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits1);
                
                //Gets error list
                try{
                    compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits1).call();
                    for (Diagnostic diagnostic : diagnostics.getDiagnostics())
                    {
                        if (diagnostic.getSource()!=null)
                        {
                            countErrors += 1;
                            String s = String.format("Error on line %d in %s: %s", diagnostic.getLineNumber(), diagnostic.getSource(), diagnostic.getMessage(Locale.ENGLISH));
                            if (!errors.contains(s))
                            {
                            errors.add(s);
                            }
                        }
                    }

                }
                catch(Exception e) {
                    log.warning("Compilation error occured: " + e.toString());
                }
                
                Trees trees = Trees.instance(task);
                
                
                JavacTask javacTask = (JavacTask) task;
                try {
                    parseResult = javacTask.parse();
                } catch (Exception e) {
                    // Parsing failed
                    log.warning("Parsing failed: "+e.toString());
                   
                    System.exit(0);
                }
                
                
                // Parses AST, gets dependencies for each java-file
                for (CompilationUnitTree compilationUnitTree : parseResult) {
                    visitor.setCurrentClass(f.getName());
                    visitor.scan(compilationUnitTree, trees);
                    JavaDependenciesInfo jdInfo = visitor.getJDInfo();
                    jdInfo.pushDelayed();
                    String currClass = jdInfo.getCurrentClass();
                    Map<String, String> resultsForClass = jdInfo.getMethods();
                    Map<String, Map> results_unit = new HashMap<String, Map>();
                    results_unit.put(currClass, resultsForClass);
                    
                    results.putAll(results_unit);
                }
                try {
                    fileManager.close();
                } catch (IOException e) {
                    log.warning(e.getLocalizedMessage());
                }
                countCurrent += 1;
            }
        } else {
            
            log.warning("No valid source files to process. Exiting from the program");
            //System.exit(0);
        }

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(errorList)); 

            if (errors.size()>0)
            {
                for (String er: errors)
                {
                    out.write(er + eol);
                }
            }
            out.close();
        
        } catch (IOException e) {
        }
    }
    public void invokeProcessor(String fileNames, boolean allProj) throws IOException {
        
        //Changes current working directory for the one with source files
        if (!fileNames.isEmpty() && !(fileNames==null))
        {
        List<File> files = new LinkedList<File>();
        String saved_cwd = System.getProperty("user.dir");
        if (allProj)
        {
            System.setProperty("user.dir", fileNames);
            // Gets the valid source files as a list
            files = getFilesAsList(System.getProperty("user.dir"));
        }
        else
        {
            files = getSingleFileAsList(fileNames);
        }
        if (!files.isEmpty() && !(files == null))
            invokeProcessor(files);
        System.setProperty("user.dir", saved_cwd);
        }

    }

    public Map<String, Map> getResults() {
        return results;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public int getErrorsCount () {
        return countErrors;
    }
    
    public int getTotal() {
        return countTotal;
    }
    
    public int getCurrent() {
        return countCurrent;
    }
    
    public int getPercent() {
        if (countTotal==0) return 0;
        return (int)(((double)countCurrent*100)/countTotal);
    }

    /**
     * Get all source files within specified directory
     * 
     * @param fileNames - directory with java files/nested directories
     * @return - list of .java File objects
     */
    private List<File> getFilesAsList(String fileNames) {
        List<File> files = new LinkedList<File>();
        String cwd = System.getProperty("user.dir");
        File folder = new File(fileNames);
        String[] list = folder.list();
        boolean flag = folder.isDirectory();
        
        if (list!=null)
        {
            for (String s : list) {
                File tmp = new File(fileNames + fs + s);
                if (tmp.isDirectory()) {
                    List<File> nestedFiles;
                    nestedFiles = getFilesAsList(fileNames + fs + s);
                    files.addAll(nestedFiles);
                } else {
                    String ext[] = s.split("\\W");
                    if (ext.length > 1 && ext[ext.length - 1].equalsIgnoreCase("java")) {
                        files.add(new File(fileNames + fs + s));
                    }
                }
            }
        }
        countTotal = files.size(); 
        return files;
    }
    
    /**
     * Get all source files within specified directory
     * 
     * @param fileNames - directory with java files/nested directories
     * @return - list of .java File objects
     */
    private List<File> getSingleFileAsList(String fileNames) {
        List<File> files = new LinkedList<File>();
        files.add(new File(fileNames));
        countTotal = 1;
        return files;
    }
}
