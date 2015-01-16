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


package org.gephi.io.plugin.jcd;

import org.gephi.io.generator.spi.GeneratorUI;
import javax.swing.*;
import org.gephi.io.generator.spi.Generator;
import java.io.File;

public class JCDGeneratorGUI implements GeneratorUI {

    private JCDGUITopComponent panel;
    
    public JCDGeneratorGUI() {
    }

    public JPanel getPanel() {
        if (panel == null) {
            panel = new JCDGUITopComponent(JCDGenerator.openPath, JCDGenerator.packFilter, JCDGenerator.classFilter);
         }   
            return panel;
    }
            
    public void setup(Generator generator) {
        
        if (panel == null) {
            panel = new JCDGUITopComponent(JCDGenerator.openPath, JCDGenerator.packFilter, JCDGenerator.classFilter);
         }  

    }

    public void unsetup() {        
        JCDGenerator.openPath = panel.jPath.getText();
        JCDGenerator.useAll = panel.jUseAll.isSelected();
        JCDGenerator.useOne = panel.jUseOne.isSelected();
        JCDGenerator.longName = panel.jLongName.isSelected();
        JCDGenerator.clearWorkspace = panel.jClearWorkspace.isSelected();       
        JCDGenerator.classFilter = panel.classFilter.toArray();
         JCDGenerator.packFilter = panel.packFilter.toArray();

         
        if("".equals(JCDGenerator.openPath))
        {
                JCDGenerator.validData = false;
                JOptionPane.showMessageDialog(this.getPanel(), 
                        "Source files path is empty.", 
                        "Error 100", 
                        JOptionPane.WARNING_MESSAGE);  
                JCDGenerator.log.info("Source files path is empty.");
        }
        
   
                if(JCDGenerator.useOne == false && !new File(JCDGenerator.openPath).isDirectory())
        {
                JCDGenerator.validData = false;
                JOptionPane.showMessageDialog(this.getPanel(), 
                        "You has not specified a folder.", 
                        "Error 101", 
                        JOptionPane.WARNING_MESSAGE);       
                JCDGenerator.log.info("You has not specified a folder.");
        }
        
        
                if(JCDGenerator.useOne == true && !new File(JCDGenerator.openPath).isFile())
        {
                JCDGenerator.validData = false;
                JOptionPane.showMessageDialog(this.getPanel(), 
                        "You has not specified a java-file.", 
                        "Error 102", 
                        JOptionPane.WARNING_MESSAGE);     
                JCDGenerator.log.info("You has not specified a java-file.");
        }
        
        panel = null;
    }
}