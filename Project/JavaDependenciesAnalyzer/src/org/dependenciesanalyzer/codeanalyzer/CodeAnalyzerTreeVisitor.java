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

import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;
import org.dependenciesanalyzer.dependenciesinfo.JavaDependenciesInfo;


/**
 * Visitor class which visits different nodes of the input source file, such as 
 * classes, imports, new classes, methods and compilation units and adds 
 * information about them to JavaDependenciesInfo structure.
 *
 */
public class CodeAnalyzerTreeVisitor extends TreeScanner<Object, Trees> {

    // Model class stores the details of the visiting class
    JavaDependenciesInfo jdInfo = new JavaDependenciesInfo();
    String currentClass;
    Logger log;
    /**
     * Visiting class, getting its name
     */
    @Override
    public Object visitClass(ClassTree classTree, Trees trees) {
        
        if (classTree.getSimpleName().toString().length() != 0)
        {
            if (classTree.getModifiers().getFlags().toString().contains("public") && classTree.getSimpleName().toString().equalsIgnoreCase(currentClass))
            {
                jdInfo.setCurrentClass(classTree.getSimpleName().toString());
                return super.visitClass(classTree, trees);
            }
            jdInfo.setPrivateClass(classTree.getSimpleName().toString());
        }
        return super.visitClass(classTree, trees);
    }
    
    /**
     * Visiting CompilationUnit - just to get current package.
     */
    @Override
    public Object visitCompilationUnit(CompilationUnitTree classTree, Trees trees)
    {
        jdInfo.setCurrentPackage(classTree.getPackageName().toString());
        return super.visitCompilationUnit(classTree, trees);
    }

    /**
     * Visiting imports - getting all obvious dependencies.
     */
    @Override
    public Object visitImport(ImportTree classTree, Trees trees) {
        jdInfo.pushImport(classTree.getQualifiedIdentifier().toString());
        return super.visitImport(classTree, trees);

    }

    /**
     * Visiting "NewClasses", where new instance of some class is created, for 
     * non-obvious cases
     */
    @Override
    public Object visitNewClass(NewClassTree classTree, Trees trees) {
        jdInfo.pushNewClass(classTree.getIdentifier().toString());
        return super.visitNewClass(classTree, trees);
    }

    /**
     * Visiting MethodInvocations - for invocations of static methods in 
     * non-obvious cases
     */
    @Override
    public Object visitMethodInvocation(MethodInvocationTree classTree, Trees trees) {
        jdInfo.pushMethod(classTree.getMethodSelect().toString());
        return super.visitMethodInvocation(classTree, trees);
    }

    /**
     * Visiting Variables - to get the list of variables and exclude them from
     * dependencies list
     */
    @Override
    public Object visitVariable(VariableTree classTree, Trees trees) {
        jdInfo.pushVariable(classTree.getName().toString());
        return super.visitVariable(classTree, trees);
    }
    
    /**
     * 
     * @return - object with all information about class and its dependencies
     */
    public JavaDependenciesInfo getJDInfo() {
        return jdInfo;
    }

    // Forwarding the list of files
    public void setFileList(List<File> flist) {
        jdInfo.setFileList(flist);

    }
    
    public void setCurrentClass(String fName)
    {
        currentClass = fName.substring(0, fName.length()-5);
    }
    
    public void setLogger(Logger lg){
        this.log = lg;
        jdInfo.setLogger(lg);
    }
}
