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

package org.dependenciesanalyzer.dependenciesinfo;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

/**
 *
 * @author craz
 */
public class JavaDependenciesInfo {

    private List<String> tmpDependencies = new ArrayList<String>();
    private List<String> unknownDependencies = new ArrayList<String>();
    private List<String> variables = new ArrayList<String>();
    private List<String> privateClasses = new ArrayList<String>();
    private Map<String, String> dependencies = new HashMap<String, String>();
    private List<String> processedFiles = new LinkedList<String>();
    private String packageName;
    private String className;
    private Logger log;
    
    /**
    * Set current package, will be used as a prefix for local class.
    * 
    * @param packName - name of the current package.
    */
    public void setCurrentPackage (String packName)
    {
        packageName = packName;
    }
    
    /**
     * Set current class, can be used as a key for dependencies set later.
     * 
     * @param clName - name of the class, being processed right now.
     */
    public void setCurrentClass (String clName)
    {
        className = clName;
    }
    
    /**
     * Return full class name, can be used as a key for dependencies set.
     * @return 
     */
    public String getCurrentClass ()
    {
        return packageName + "." + className;
    }

    /**
     * Add private class, used in current class.
     * @param privateClass - name of the class 
     */
    public void setPrivateClass (String privateClass)
    {
        privateClasses.add(privateClass);
    }
    
    /**
     * Add class, whom static method was called, to a "waiting" list
     * @param method - called method name like "class.method"
     */
    public void pushVariable(String variable) {
        variables.add(variable);
    }
    
    /**
     * Add class, whom static method was called, to a "waiting" list
     * @param method - called method name like "class.method"
     */
    public void pushMethod(String method) {
        String tmp[] = method.split("\\.");
        if (tmp.length > 1 && (!dependencies.containsKey(tmp[0])) && processedFiles.contains(tmp[0])) {
            tmpDependencies.add(tmp[0]);
        }
        else if (tmp.length > 1 && !dependencies.containsKey(tmp[0]))
        {
            unknownDependencies.add(tmp[0]);
        }
        
    }

    /**
     * Add class, whom instance was created, to a "waiting" list 
     * (same as in pushMethod)
     * @param clazz - name of class
     */
    public void pushNewClass(String clazz) {
        if ((!dependencies.containsKey(clazz)) && processedFiles.contains(clazz)) {
            tmpDependencies.add(clazz);
        }
        else if (!dependencies.containsKey(clazz))
        {
            dependencies.put(clazz, "<unknown>." + clazz);
        }
    }
    
    /**
     * Add to the dependencies hash all imported classes:
     *  [key-value]: [class-package.class]
     * @param myImport - imported class
     */
    public void pushImport(String myImport) {
        String tmp[] = myImport.split("\\.");
        if (tmp.length > 1 && (!dependencies.containsKey(tmp[tmp.length - 1])) && !tmp[tmp.length - 1].equals("*")) {
            dependencies.put(tmp[tmp.length - 1], myImport);
        }
        else if (tmp.length > 1 && (!dependencies.containsKey(myImport)) && tmp[tmp.length - 1].equals("*")) {
            dependencies.put(myImport, myImport);
        }
        else if (tmp.length == 1 && (!dependencies.containsKey(myImport))) {
            dependencies.put(myImport, myImport);
        }
    }

    /**
     * Add classes from "waiting list" to the dependencies hash.
     */
    public void pushDelayed(){
        for (String s : tmpDependencies)
            if ((!dependencies.containsKey(s)) && processedFiles.contains(s))
                dependencies.put(s, packageName + "." + s);
        
        for (String s : privateClasses)
            dependencies.put(s, packageName + "." + className + "." + s);
        
        for (String s: unknownDependencies)
            if ((!dependencies.containsKey(s)) && (!variables.contains(s)))
                dependencies.put(s, "<unknown>." + s);
    }
    
    
    /**
     * 
     * @return - the dependencies hash
     */
    public Map<String, String> getMethods() {

        return dependencies;
    }

    /**
     * Make a list of source files in the project. Not sure, if still is really 
     * needed.
     * @param flist 
     */
    public void setFileList(List<File> flist) {
        for (File f : flist) {
            String tmp[] = f.toString().split("\\W");
            processedFiles.add(tmp[tmp.length - 2]);
            //System.out.println(tmp[tmp.length-2]);
        }
    }
   
    public void setLogger(Logger lg)
    {
        this.log = lg;
    }
}

