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

import java.io.IOException;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;
import org.gephi.io.generator.spi.Generator;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.generator.spi.GeneratorUI;
import org.dependenciesanalyzer.codeanalyzer.CodeAnalyzerController;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.Lookup;
import java.util.logging.*;
import javax.swing.JOptionPane;
import javax.swing.*;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.utils.progress.Progress;

class ProgressThread implements Runnable {
    
    Thread runner;
    ProgressTicket progressTick;
    CodeAnalyzerController controller;
    boolean Active = true;
    
    public ProgressThread(ProgressTicket t, CodeAnalyzerController c) {
        progressTick = t;
        controller = c;
        runner = new Thread(this, "ProgressThread");
        runner.start();
    }
    
    public void run() {
        while (Active) {
            if (progressTick != null && controller != null) {
                double cur = controller.getCurrent();
                double total = controller.getTotal();
                double perc = 0;
                
                if (total != 0) {
                    perc = cur / total * 100;                    
                    Progress.progress(progressTick, (int)perc);
                    //System.out.println("Progress Percent: " + String.valueOf(perc) + " "+ controller.getCurrent() + " " + controller.getTotal());
                    if (perc > 98) {
                        this.deactivate();
                    }
                }
                
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                }
            }
        }
    }
    
    public void deactivate() {
        Active = false;
    }
}

@ServiceProvider(service = Generator.class)
public class JCDGenerator implements Generator {
    
    String eol = System.getProperty("line.separator");
    String userDirectory = System.getProperty("user.home");
    String logPath = userDirectory + System.getProperty("file.separator") + "JCDPluginLog.xml";
    String errorLogPath = userDirectory + System.getProperty("file.separator") + "JDALog.txt";
    protected ProgressTicket progressTick;
    protected boolean cancel = false;
    static Logger log;
    //  Управляющие данные
    static boolean validData = true;
    static String openPath = System.getProperty("user.home");;
    static boolean useAll = false;
    static boolean useAllProj = false;
    static boolean useOne = false;
    static boolean longName = true;
    static boolean clearWorkspace = true;
    static Object[] classFilter;
    static Object[] packFilter;
    ProgressThread pth;
    
    public JCDGenerator() {
        super();
        
        try {
            FileHandler hand = new FileHandler(logPath);
            log = Logger.getLogger("JCDPluginLog");
            log.addHandler(hand);
        } catch (IOException e) {
            System.out.println("Log init failed.");
            e.printStackTrace();
        }
    }
    
    public void clear() {
        try {
            GraphController gc = Lookup.getDefault().lookup(GraphController.class);
            GraphModel graphModel = gc.getModel();
            Graph graph = graphModel.getGraph();
            if (graph != null) {
                graph.clear();
            }
        } catch (Exception e) {
            log.warning(e.toString());
            
        }
    }
    
    public void normalizeFilter() {
        if (packFilter != null) {
            if (packFilter.length > 0) {
                int i = 0;
                for (Object f : packFilter) {
                    f = f.toString().split(" /!@#$%^&()-+=':;/|\\,")[0];
                    
                    if (f.toString().contains("*")) {
                        f = f.toString().substring(0, f.toString().indexOf("*"));
                    }
                    
                    packFilter[i] = f;
                    i++;
                }
                
                log.info("Normalization for pack filter");
                for (Object f : packFilter) {
                    log.info(f.toString());
                }
            }
        }
        
        if (classFilter != null) {
            if (classFilter.length > 0) {
                int i = 0;
                for (Object f : classFilter) {
                    f = f.toString().split(" /!@#$%^&()-+=':;/|\\,.*")[0];
                    classFilter[i] = f;
                    i++;
                }
                
                log.info("Normalization for class filter");
                for (Object f : classFilter) {
                    log.info(f.toString());
                }
            }
        }
    }
    
    public boolean filter(String key) {
        
        if (packFilter != null) {
            if (packFilter.length > 0) {
                for (Object f : packFilter) {
                    
                    if (key.startsWith(f.toString())) {
                        log.info("Filter " + key);
                        return true;
                    }
                }
            }
        }
        
        if (classFilter != null) {
            if (classFilter.length > 0) {
                for (Object f : classFilter) {
                    if (key.endsWith(f.toString())) {
                        log.info("Filter " + key);
                        return true;
                    }
                }
            }
        }

        //log.info("Filter " + key + " no");
        return false;
    }
    
    public void generate(ContainerLoader container) {
        
        log.info("Call: generate graph");
        
        if (!validData) {
            return;
        }
        
        
        Progress.setDisplayName(progressTick, "Clearing workspace...");
        
        if (clearWorkspace == true) {
            clear();
        }
        
        Progress.setDisplayName(progressTick, "Analyzer is running...");
        int units = 100;
        Progress.start(progressTick, units);
        
        Map<String, Map> dependencies = new HashMap<String, Map>();
        CodeAnalyzerController controller = new CodeAnalyzerController(log, errorLogPath);
        
        
        try {
            pth = new ProgressThread(progressTick, controller);
        } catch (Exception e) {
            log.warning(e.getMessage());
        }
        
        try {
            if (useAll) {
                // Обработка исходных кодов
                
                controller.invokeProcessor(openPath, true); //Передача пути к файлам для анализа
                dependencies = controller.getResults();
                
            } else {
                if (useOne) {
                    
                    controller.invokeProcessor(openPath, false); //Передача пути к файлам для анализа
                    dependencies = controller.getResults();
                    
                }
            }
        } catch (Exception ex) {
            pth.deactivate();
            log.warning(ex.getMessage());
        }
        
        
        if (pth != null) {
            pth.deactivate();
        }
        
        Progress.setDisplayName(progressTick, "Graph rendering...");        
        int totalUnit = dependencies.keySet().size();
        normalizeFilter();
        
        for (String key : dependencies.keySet()) {
            
                        
            if (filter(key)) {
                continue;
            }
            
                      
            NodeDraft n = container.getNode(key);
            Map<String, String> dep_map = dependencies.get(key);
            if (longName == false) {
                String[] key_split = key.split("\\W");
                n.setLabel(key_split[key_split.length - 1]);
            } else {
                n.setLabel(key);
            }
            container.addNode(n);
            for (String dep_key : dep_map.keySet()) {
                if (filter(dep_map.get(dep_key))) {
                    continue;
                }
                
                NodeDraft tgt = container.getNode(dep_map.get(dep_key));
                if (longName == false) {
                    tgt.setLabel(dep_key);
                } else {
                    tgt.setLabel(dep_map.get(dep_key));
                }
                
                container.addNode(tgt);
                if (!container.edgeExists(n, tgt)) {
                    EdgeDraft e = container.factory().newEdgeDraft();
                    e.setSource(n);
                    e.setTarget(tgt);
                    container.addEdge(e);
                }
                
                
            }
            
        }

        if (controller.getErrorsCount() > 0) {
            String temp = "error";
            if (controller.getErrorsCount() > 1) {
                temp = temp + "s";
            }
            Progress.setDisplayName(progressTick, controller.getErrorsCount() + " " + temp);
            
            temp = "Code аnalyzing was finished with " + controller.getErrorsCount() + " " + temp + "."
                    + eol + "Log-file was saved to " + eol + this.errorLogPath;
            
            JOptionPane.showMessageDialog(null, temp,
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            JCDGenerator.log.info(temp);
        }
        
        Progress.finish(progressTick);
    }
    
    public String getName() {
        return "Java Class Dependency";
    }
    
    public GeneratorUI getUI() {
        GeneratorUI frame = new JCDGeneratorGUI();
        log.info("Call: Plugin GUI");
        return frame;
    }
    
    public boolean cancel() {
        cancel = true;
        return true;
    }
    
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTick = progressTicket;
    }
}
