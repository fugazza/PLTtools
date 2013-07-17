/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package plttools;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import plttools.optimizer.AbstractOptimizer;
import plttools.optimizer.AntColonyOptimizer;
import plttools.optimizer.CorrectorOptimizer;
import plttools.optimizer.GreedyOptimizer;
import plttools.optimizer.ModifiedGreedyOptimizer;

/**
 *
 * @author Vláďa
 */
public class PLTfile {

    private StringBuilder rawPLT;
    private PLTdata pltData;
    private PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);
    private PLTdata optimizedFile = null;
    private PropertyChangeListener parent;
    private SettingsData settings;
    
    public void readPLTfromFile(File file) {
        BufferedReader reader = null;
        try {
//            System.out.println("reading started");
            reader = new BufferedReader(new FileReader(file));
            StringBuilder contents = new StringBuilder();
            String text = "";
            while ((text = reader.readLine()) != null) {
                contents.append(text).append(System.getProperty("line.separator"));            
            }
//            System.out.println("file read");
            setRawPLT(contents);
//            System.out.println("start parsing");
            parseRaw();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PLTfile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PLTfile.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                Logger.getLogger(PLTfile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
//        System.out.println("reading finished");
    }

    private void parseRaw() {
        String prikaz;
        boolean penUp = true;
        // spočítat čáry a přejezdy
        int pocetPrejezdu = 0;
        int pocetCar = 0;
//        System.out.println("parseRaw - start parsing");
        StringTokenizer st0 = new StringTokenizer(rawPLT.toString(),";");
        while (st0.hasMoreTokens()) {
            prikaz = st0.nextToken();
            if (prikaz.startsWith("PUPA") || (penUp && prikaz.startsWith("PA"))) {
                int commandLength = (prikaz.startsWith("PUPA")) ? 4 : 2;
                StringTokenizer st2 = new StringTokenizer(prikaz.substring(commandLength),",");
                while (st2.hasMoreTokens()) {
                    st2.nextToken();
                    st2.nextToken();
                    pocetPrejezdu++;
                }
                penUp = true;
            } else if (prikaz.equals("PU")) {
                penUp = true;
            } else if (prikaz.startsWith("PDPA") || (!penUp && prikaz.startsWith("PA"))) {
                int commandLength = (prikaz.startsWith("PDPA")) ? 4 : 2;
                StringTokenizer st2 = new StringTokenizer(prikaz.substring(commandLength),",");                        
                while (st2.hasMoreTokens()) {
                    st2.nextToken();
                    st2.nextToken();
                    pocetCar++;
                }
                penUp = false;
            } else if (prikaz.equals("PD")) {
                penUp = false;
            }
        }
//        System.out.println("parseRaw - lines counted: " + pocetCar);
        
        // teď teprve získat jednotlivé hodnoty
        pltData = new PLTdata();
        pltData.setLineCount(pocetCar);
        int x2a;
        int y2a;
        int i = 0;
        byte pen = 0;
        int lastX = 0, lastY = 0;
               
        penUp = true;
//        System.out.println("parseRaw - before tokenizer");
        StringTokenizer st = new StringTokenizer(rawPLT.toString(),";");
//        System.out.println("parseRaw - tokenizer started");
        while (st.hasMoreTokens()) {
            prikaz = st.nextToken();
//            System.out.println("prikaz: " +prikaz);

            if (prikaz.startsWith("PUPA") || (penUp && prikaz.startsWith("PA"))) {
                int commandLength = (prikaz.startsWith("PUPA")) ? 4 : 2;
                StringTokenizer st2 = new StringTokenizer(prikaz.substring(commandLength),",");
                while (st2.hasMoreTokens()) {
                    x2a = Integer.parseInt(st2.nextToken());
                    y2a = Integer.parseInt(st2.nextToken());
                    
                    lastX = x2a;
                    lastY = y2a;
                }
                penUp = true;
            } else if (prikaz.equals("PU")) {
                penUp = true;
            } else if (prikaz.startsWith("PDPA") || (!penUp && prikaz.startsWith("PA"))) {
                int commandLength = (prikaz.startsWith("PDPA")) ? 4 : 2;
                StringTokenizer st2 = new StringTokenizer(prikaz.substring(commandLength),",");                        
                while (st2.hasMoreTokens()) {
                    x2a = Integer.parseInt(st2.nextToken());
                    y2a = Integer.parseInt(st2.nextToken());
                    pltData.addLine(lastX, lastY, x2a, y2a, pen);
                    lastX = x2a;
                    lastY = y2a;
                    i++;
//                    System.out.println("parseRaw - "+i+" tokens from "+ pocetCar +" read");
                }
                penUp = false;
            } else if (prikaz.equals("PD")) {
                penUp = false;
            } else if (prikaz.startsWith("SP")) {
                if (prikaz.length() > 2) {
                    pen = Byte.parseByte(prikaz.substring(2));
                } else {
                    pen = 0;
                }
            }
        }

        pltData.calculateStats();
//        System.out.println("before property fire");
        propertySupport.firePropertyChange("fileRead", false, true);
//        System.out.println("after property fire");
//        System.out.println("počet bodů = " + pocetBodu);
//        for (int l=0; l<25; l++) {
//            System.out.println("line "+l+": from "+lines_1[l]+"["+point_x[lines_1[l]]+","+point_y[lines_1[l]]+"] to "+lines_2[l]+"["+point_x[lines_2[l]]+","+point_y[lines_2[l]]+"]");
//        }
    }
    
    public void optimizePLT(int alghoritm) {
        AbstractOptimizer optimizer;
        switch(alghoritm) {
            case 3:
//                System.out.println("Corrector");
                optimizer = new CorrectorOptimizer();
                break;
            case 2:
//                System.out.println("Ant Colony alghoritm");
                optimizer = new AntColonyOptimizer();
                break;
            case 1:
                optimizer = new ModifiedGreedyOptimizer();
//                System.out.println("Greedy alghoritm");
                break;
            case 0:
            default:
                optimizer = new GreedyOptimizer();
//                System.out.println("modified Greedy alghoritm");
                break;
        }      
        optimizer.addPropertyChangeListener(parent);
        optimizer.setSettings(settings);
        optimizer.setData(pltData);
//        System.out.println("optimization before start");
        int linesBeforeOptimization = pltData.getPocetCar();
        try {
            optimizedFile = optimizer.optimize();
            optimizedFile.calculateStats();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Following error occured during optimization:\n" + e.toString(),
                    "Optimization error",
                    JOptionPane.ERROR_MESSAGE);
        }
//        System.out.println("optimization finished");
        int linesAfterOptimization = optimizedFile.getPocetCar();
        if (!optimizer.changesLineCount() && (linesBeforeOptimization != linesAfterOptimization)) {
            JOptionPane.showMessageDialog(null,
                    "Lines count after optimization ("+linesAfterOptimization+") does not match line count before optimization ("+linesBeforeOptimization+").\nSomething is terribly wrong, because the alghoritm should not change count of lines.",
                    "Error: Lines counte before and after optimization does not macth.",
                    JOptionPane.ERROR_MESSAGE);
        }
        propertySupport.firePropertyChange("progressFinished", false, true);
    }
    
    public PLTdata getOptimizedPLT() {
        return optimizedFile;
    }
    
    public void saveToFile(File file) {
        PrintWriter bw = null;
        try {
            bw = new PrintWriter(new FileWriter(file));
            bw.write("IN;SC;PU;RO0;IP;IW;VS15");
            int lastX = -1, lastY = -1, lastPen = -1;
            byte pens[] = pltData.getPens();
            int point_x[] = pltData.getPoint_x();
            int point_y[] = pltData.getPoint_y();
            int lines_1[] = pltData.getLines_1();
            int lines_2[] = pltData.getLines_2();
            for(int i=0; i<pltData.getPocetCar(); i++) {
                if (pens[i] != lastPen) {
                    bw.write(";SP"+pens[i]);
                    lastPen = pens[i];
                }
                if (point_x[lines_1[i]]!=lastX || point_y[lines_1[i]]!=lastY) {
                    bw.write(";PUPA"+point_x[lines_1[i]]+","+point_y[lines_1[i]]+";PDPA"+point_x[lines_2[i]]+","+point_y[lines_2[i]]);
                } else {
                    bw.write(","+point_x[lines_2[i]]+","+point_y[lines_2[i]]);
                }          
                lastX = point_x[lines_2[i]];
                lastY = point_y[lines_2[i]];
            }
            bw.write(";PU;PA0,0;SP;");
        } catch (IOException ex) {
            Logger.getLogger(PLTfile.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            bw.close();
        }
    }
       
    public void setRawPLT(StringBuilder rawPLT) {
        this.rawPLT = rawPLT;
    }

    public StringBuilder getRawPLT() {
        return rawPLT;
    }

    @Override
    protected Object clone() {
        PLTfile p = new PLTfile();
        p.rawPLT = rawPLT;
        p.pltData = (PLTdata) pltData.clone();
        return p;
    }
        
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
        parent = listener;
    }

    public void setParent(PropertyChangeListener parent) {
        this.parent = parent;
    }

    public PLTdata getPltData() {
        return pltData;
    }

    public void setPltData(PLTdata pltData) {
        this.pltData = pltData;
    }

    public void setSettings(SettingsData settings) {
        this.settings = settings;
    }

}
