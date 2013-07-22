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

    private File myFile;
    private StringBuilder rawPLT;
    private PLTdata[] pltData;
    private PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);
    private PLTdata optimizedPltData[];
    private PropertyChangeListener parent;
    private SettingsData settings;
    
    public void readPLTfromFile(File file) {
        myFile = file;
        BufferedReader reader = null;
        try {
            System.out.println("reading started");
            reader = new BufferedReader(new FileReader(file));
            StringBuilder contents = new StringBuilder();
            String text = "";
            while ((text = reader.readLine()) != null) {
                contents.append(text).append(System.getProperty("line.separator"));            
            }
            System.out.println("file read");
            setRawPLT(contents);
            System.out.println("start parsing");
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
        // count number of lines, points and pens
        int maxPens = 7;
        int countTravels[] = new int[maxPens];
        int countLines[] = new int[maxPens];
        boolean penUp = true;
        int activePen = 0;
        boolean pensInPlot[] = new boolean[maxPens];
        
        System.out.println("parseRaw - start parsing");
        StringTokenizer st0 = new StringTokenizer(rawPLT.toString(),";");
        while (st0.hasMoreTokens()) {
            prikaz = st0.nextToken();
            if (prikaz.startsWith("PUPA") || (penUp && prikaz.startsWith("PA"))) {
                int commandLength = (prikaz.startsWith("PUPA")) ? 4 : 2;
                StringTokenizer st2 = new StringTokenizer(prikaz.substring(commandLength),",");
                while (st2.hasMoreTokens()) {
                    st2.nextToken();
                    st2.nextToken();
                    countTravels[activePen]++;
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
                    countLines[activePen]++;
                }
                penUp = false;
            } else if (prikaz.equals("PD")) {
                penUp = false;
            } else if (prikaz.startsWith("SP")) {
                if (prikaz.length() > 2) {
                    activePen = Byte.parseByte(prikaz.substring(2));
                } else {
                    activePen = 0;
                }
                if (!pensInPlot[activePen]) {
                    pensInPlot[activePen] = true;
                }
            }
        }
        
        // recalculate count of pens and verify if they really contain any line
        int countPens = 0;
        for(int k=0; k<maxPens; k++) {
            if (countLines[k] > 0) {
                countPens++;                            
            }
        }
        
        // and since now acquire plot values from source file
        pltData = new PLTdata[countPens];
        optimizedPltData = new PLTdata[countPens];
        int penToIndex[] = new int[maxPens];
        // initialize plotData array
        int l=0;
        for(int k=0; k<maxPens; k++) {
            System.out.println("parseRaw - " + countLines[k] + " lines for pen " + k);
            if (pensInPlot[k] && countLines[k] > 0) {
                pltData[l] = new PLTdata();
                pltData[l].setLineCount(countLines[k]);
                optimizedPltData[l] = new PLTdata();
                penToIndex[k] = l;
                l++;
            }
        }
        
        int x2a;
        int y2a;
        int i = 0;
        int lastX = 0, lastY = 0;
               
        penUp = true;
        System.out.println("parseRaw - before tokenizer");
        StringTokenizer st = new StringTokenizer(rawPLT.toString(),";");
        System.out.println("parseRaw - tokenizer started");
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
                    pltData[penToIndex[activePen]].addLine(lastX, lastY, x2a, y2a);
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
                    activePen = Byte.parseByte(prikaz.substring(2));
                } else {
                    activePen = 0;
                }
                pltData[penToIndex[activePen]].setPen((byte) activePen);
            }
        }

        for (PLTdata p: pltData) {
            p.addPropertyChangeListener(parent);
            p.calculatePathLengths();
            p.calculateStats();            
        }
        System.out.println("before property fire");
        propertySupport.firePropertyChange("fileRead", false, true);
        System.out.println("after property fire");
//        System.out.println("počet bodů = " + pocetBodu);
//        for (int l=0; l<25; l++) {
//            System.out.println("line "+l+": from "+lines_1[l]+"["+point_x[lines_1[l]]+","+point_y[lines_1[l]]+"] to "+lines_2[l]+"["+point_x[lines_2[l]]+","+point_y[lines_2[l]]+"]");
//        }
    }
    
    public void optimizePLT(int alghoritm, int pen) {
        AbstractOptimizer optimizer;
        switch(alghoritm) {
            case 3:
//                System.out.println("Corrector");
                optimizer = new CorrectorOptimizer();
                ((CorrectorOptimizer) optimizer).boundingBox = pltData[0].getBoundingBox();
                for (PLTdata p: pltData) {
                    ((CorrectorOptimizer) optimizer).boundingBox.add(p.getBoundingBox());
                }
                break;
            case 2:
//                System.out.println("Ant Colony alghoritm");
                optimizer = new AntColonyOptimizer();
                break;
            case 1:
                optimizer = new ModifiedGreedyOptimizer();
//                System.out.println("modified Greedy alghoritm");
                break;
            case 0:
            default:
                optimizer = new GreedyOptimizer();
//                System.out.println("Greedy alghoritm");
                break;
        }      
        optimizer.addPropertyChangeListener(parent);
        optimizer.setSettings(settings);
        System.out.println("optimization before start");
        int linesBeforeOptimization = countTotalLines(pltData);
        try {
            optimizedPltData = new PLTdata[pltData.length];
            for (int i=0; i<pltData.length; i++) {
                if (pen == -1 || pltData[i].getPen() == pen) {
                    optimizer.setData(pltData[i]);
                    optimizedPltData[i] = optimizer.optimize();
                    optimizedPltData[i].addPropertyChangeListener(parent);
                    optimizedPltData[i].calculateStats();
                } else {
                    optimizedPltData[i] = pltData[i];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Following error occured during optimization:\n" + e.toString(),
                    "Optimization error",
                    JOptionPane.ERROR_MESSAGE);
        }
        System.out.println("optimization finished");
        int linesAfterOptimization = countTotalLines(optimizedPltData);
        if (!optimizer.changesLineCount() && (linesBeforeOptimization != linesAfterOptimization)) {
            JOptionPane.showMessageDialog(null,
                    "Lines count after optimization ("+linesAfterOptimization+") does not match line count before optimization ("+linesBeforeOptimization+").\nSomething is terribly wrong, because the alghoritm should not change count of lines.",
                    "Error: Lines counte before and after optimization does not macth.",
                    JOptionPane.ERROR_MESSAGE);
        }
        propertySupport.firePropertyChange("progressFinished", false, true);
    }
    
    public PLTdata[] getOptimizedPLT() {
        return optimizedPltData;
    }
    
    private int countTotalLines(PLTdata pd[]) {
        int countLines = 0;
        for (PLTdata p: pd) {
            countLines += p.getPopulatedLines();
            System.out.println("pen " + p.getPen() + " has " + p.getPopulatedLines() + " lines.");
        }
        return countLines;
    }
    
    public void saveToFile(File file) {
        PrintWriter bw = null;
        try {
            bw = new PrintWriter(new FileWriter(file));
            bw.write("IN;SC;PU;RO0;IP;IW;VS15");
            for (PLTdata p: pltData) {
                int lastX = -1, lastY = -1;
                int point_x[] = p.getPoint_x();
                int point_y[] = p.getPoint_y();
                int lines_1[] = p.getLines_1();
                int lines_2[] = p.getLines_2();
                bw.write(";SP"+p.getPen());
                for(int i=0; i<p.getPocetCar(); i++) {
                    if (point_x[lines_1[i]]!=lastX || point_y[lines_1[i]]!=lastY) {
                        bw.write(";PUPA"+point_x[lines_1[i]]+","+point_y[lines_1[i]]+";PDPA"+point_x[lines_2[i]]+","+point_y[lines_2[i]]);
                    } else {
                        bw.write(","+point_x[lines_2[i]]+","+point_y[lines_2[i]]);
                    }          
                    lastX = point_x[lines_2[i]];
                    lastY = point_y[lines_2[i]];
                }
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
        p.pltData = new PLTdata[pltData.length];
        for (int i = 0; i < pltData.length; i++) {
            p.pltData[i] = (PLTdata) pltData[i].clone();            
        }
        return p;
    }
        
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
        parent = listener;
    }

    public void setParent(PropertyChangeListener parent) {
        this.parent = parent;
    }

    public PLTdata[] getPltData() {
        return pltData;
    }

    public void setPltData(PLTdata pltData[]) {
        this.pltData = pltData;
    }

    public void setSettings(SettingsData settings) {
        this.settings = settings;
    }

    public int getLinesCount() {
        System.out.println("returning Lines count to superior function");
        return countTotalLines(pltData);
    }
    
    public int getLinesLength() {
        int linesLength = 0;
        for (PLTdata p: pltData) {
            linesLength += p.getDelkaCar();
        }
        return linesLength;        
    }

    public int getTravelsCount() {
        int travelsCount = 0;
        for (PLTdata p: pltData) {
            travelsCount += p.getPocetPrejezdu();
        }
        return travelsCount;        
    }

    public int getTravelsLength() {
        int travelsLength = 0;
        for (PLTdata p: pltData) {
            travelsLength += p.getDelkaPrejezdu();
        }
        return travelsLength;        
    }

    public File getFile() {
        return myFile;
    }   

    public long getRequiredMemory(int alghoritm) {
        int countPoints = 0;
        for (PLTdata p: pltData) {
            countPoints += p.getPocetBodu();
        }
        int countLines = getLinesCount();
        switch(alghoritm) {
            case 3:
//                System.out.println("Corrector - required memory");
                return 2*4*10*countLines + 2*4*countPoints + 50*1024*1024;
            case 2:
//                System.out.println("Ant Colony alghoritm - required memory");
                return 2*4*10*countLines + countLines/8 + countPoints/8 + 4*countPoints + 2*4*countPoints*countPoints + 50*1024*1024;
            case 1:
//                System.out.println("modified Greedy alghoritm - required memory");
                return 2*4*10*countLines + 2*4*countPoints + 50*1024*1024;
            case 0:
            default:
//                System.out.println("Greedy alghoritm - required memory");
                return 2*4*10*countLines + 2*4*countPoints + 50*1024*1024;
        }      
    }
}
