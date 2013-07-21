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

/**
 *
 * @author Vláďa
 */
public class PLTfile {

    private final int alghoritm = 3;

    private StringBuilder rawPLT;
    private int max_x = 0;
    private int max_y = 0;
    private int point_x[], point_y[], lines_at_point[];
    private int lines_1[], lines_2[], pens[], status[];
    private float distances[][];
    private int pocetCar = 0;
    private int pocetPrejezdu = 0;
    private int pocetBodu = 0;
    private double delkaCar = 0.0;
    private double delkaPrejezdu = 0.0;
    private PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);
    private PLTfile optimizedFile = null;
    
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
        pocetCar = 0;
        pocetPrejezdu = 0;
        pocetBodu = 0;
        delkaPrejezdu = 0;
        delkaCar = 0;
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
//        System.out.println("parseRaw - lines counted");
        
        // teď teprve získat jednotlivé hodnoty
        pens = new int[pocetCar];
        lines_1 = new int[pocetCar];
        lines_2 = new int[pocetCar];
        status = new int[pocetCar];
        point_x = new int[2*pocetCar+1];
        point_y = new int[2*pocetCar+1];
        lines_at_point = new int[2*pocetCar+1];
        int x2a;
        int y2a;
        int i = 0;
        int firstPoint = getPointId(0,0,true);
        int point_id;
        int pen = 0;
        int lastX = 0, lastY = 0;
        
        for (int j=0; j<2*pocetCar+1; j++) {
            lines_at_point[j] = 0;
        }
        
        penUp = true;
//        System.out.println("parseRaw - before tokenizer");
        StringTokenizer st = new StringTokenizer(rawPLT.toString(),";");
//        System.out.println("parseRaw - tokenizer started");
        while (st.hasMoreTokens()) {
            prikaz = st.nextToken();

            if (prikaz.startsWith("PUPA") || (penUp && prikaz.startsWith("PA"))) {
                int commandLength = (prikaz.startsWith("PUPA")) ? 4 : 2;
                StringTokenizer st2 = new StringTokenizer(prikaz.substring(commandLength),",");
                while (st2.hasMoreTokens()) {
                    x2a = Integer.parseInt(st2.nextToken());
                    y2a = Integer.parseInt(st2.nextToken());
                    
                    delkaPrejezdu += Math.sqrt(Math.pow(x2a - lastX, 2) + Math.pow(y2a - lastY, 2));
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
                    firstPoint = getPointId(lastX, lastY, true);
                    point_id = getPointId(x2a, y2a, true);
                    pens[i] = pen;
                    lines_1[i] = firstPoint;
                    lines_2[i] = point_id;
                    lines_at_point[point_id]++;
                    lines_at_point[firstPoint]++;
                    delkaCar += Math.sqrt(Math.pow(x2a - lastX, 2) + Math.pow(y2a - lastY, 2));
                    lastX = x2a;
                    lastY = y2a;
                    i++;
                    //System.out.println("parseRaw - "+i+" tokens read");
                }
                penUp = false;
            } else if (prikaz.equals("PD")) {
                penUp = false;
            } else if (prikaz.startsWith("SP")) {
                if (prikaz.length() > 2) {
                    pen = Integer.parseInt(prikaz.substring(2));
                } else {
                    pen = 0;
                }
            }
        }

//        System.out.println("before property fire");
        propertySupport.firePropertyChange("fileRead", false, true);
//        System.out.println("after property fire");
        calculateDistances();
        for (int k=0; k < pocetCar; k++) {
            if (status[k] <= 0) {
                status[k] = checkStatus(k);
            }
//            System.out.println("status of " + k + " = "+status[k]+"; pen = " +pens[k]);
        }
//        System.out.println("počet bodů = " + pocetBodu);
//        for (int l=0; l<25; l++) {
//            System.out.println("line "+l+": from "+lines_1[l]+"["+point_x[lines_1[l]]+","+point_y[lines_1[l]]+"] to "+lines_2[l]+"["+point_x[lines_2[l]]+","+point_y[lines_2[l]]+"]");
//        }
    }
    
    private int getPointId(int x, int y, boolean createNew) {
//        for (int i=0; i < 2*pocetCar+1; i++) {
//            if (point_x[i] == x && point_y[i] == y) {
//                return i;
//            }
//        }
        if (createNew) {
            if (x > max_x) {
                max_x = x;
            }
            if (y > max_y) {
                max_y = y;
            }
            point_x[pocetBodu] = x;
            point_y[pocetBodu] = y;
            pocetBodu++;
            return pocetBodu - 1;
        } else {
            return -1;
        }
    }
    
    /**
     * 1 = konec v bodě 1
     * 2 = konec v bodě 2
     * 3 = součást uzavřené smyčky
     * 4 = součást polyčáry
     * 5 = samostatná čára (konec na obou stranách)
     * 6 = chyba
     * @param index 
     * @param loopStart
     * @return status
     */
    private int checkStatus(int index) {
        
        int countOn1 = lines_at_point[lines_1[index]];
        int countOn2 = lines_at_point[lines_2[index]];
        if (countOn1==2 && countOn2==2) {
            if (findLoop(index)) {
                return 3;
            } else {
                return 4;                        
            }
        }
        if (countOn1!=2 && countOn2!=2) {
            return 5;
        }
        if (countOn1==2 && countOn2!=2) {
            return 2;
        }
        if (countOn1!=2 && countOn2==2) {
            return 1;
        }
        return 6;
    }

    
    private boolean findLoop(int startIndex) {
        int foundPoint = lines_1[startIndex];
        int x2a;
        int y2a;
        int ka = startIndex;
        int numAt1;
        int numAt2;
        boolean hledatDalsi = true;
        if (lines_at_point[lines_1[startIndex]]<2) {
            return false;
        }
        while (hledatDalsi) {
            x2a = point_x[foundPoint];
            y2a = point_y[foundPoint];
            hledatDalsi = false;
            for (int i=0; i<pocetCar; i++) {
                if (i!=ka) {
                    if (point_x[lines_1[i]]==x2a && point_y[lines_1[i]]==y2a) {
                        numAt2 = lines_at_point[lines_2[i]];
                        if (numAt2 != 2) {
                            return false;
                        } else {
                            foundPoint = lines_2[i];
                            ka = i;
                            hledatDalsi = true;
                            break;
                        }
                    }
                    if (point_x[lines_2[i]]==x2a && point_y[lines_2[i]]==y2a) {
                        numAt1 = lines_at_point[lines_1[i]];
                        if (numAt1 != 2) {
                            return false;
                        } else {
                            foundPoint = lines_1[i];
                            ka = i;
                            hledatDalsi = true;
                            break;
                        }
                    }
                }
            }
            if (ka == startIndex || status[ka] == 3) {
                return true;
            }
        }
        return false;
    } 
    
    public void optimizePLT() {
        switch(alghoritm) {
            case 3:
                optimizedFile = optimizationAntColony();
            case 2:
                optimizedFile = optimizationGreedy();
            case 1:
            default:
                optimizedFile = optimizationGreedyModified();
        }        
        propertySupport.firePropertyChange("progressFinished", false, true);
    }
    
    public PLTfile getOptimizedPLT() {
        return optimizedFile;
    }
    
    private PLTfile optimizationAntColony() {
        propertySupport.firePropertyChange("progressMessage", null, "optimization started");
        System.out.println("optimization started");
        PLTfile p = (PLTfile) clone();
        calculateDistances();
        System.out.println("distances calculated");
        int numProcessed;
        int numLinesProcessed = 0;
        double maxAttractivity;
        boolean linesProcessed[] = new boolean[pocetCar];
        int antPath[] = new int[pocetBodu];
        double antPathLength;
        float attractivity[][] = new float[pocetBodu][pocetBodu];
        System.out.println("attractivity array allocated");
        float pheromones[][] = new float[pocetBodu][pocetBodu];
        System.out.println("pheromones array allocated");
        for (int i=0; i<pocetBodu;i++) {
            for (int j=i+1; j<pocetBodu;j++) {
                pheromones[i][j] = 1.0f;
                pheromones[j][i] = 1.0f;
            }
        }

        float attract;
        int i, j, k;
        int lastPoint;
        int antsCount = 10;
        for (k=0; k<antsCount; k++) {
            if (k==(antsCount-1)) {
                lastPoint = 0;
            } else {
                lastPoint = (int) (Math.random()*pocetBodu);
            }
            propertySupport.firePropertyChange("progressMessage", null, "Ant "+(k+1)+" from "+(antsCount)+"is running");
            System.out.println("Ant "+k+"; startpoint = "+lastPoint);
            int nextPoint = 0;
            boolean processed[] = new boolean[pocetBodu];
            processed[lastPoint] = true;
            // generate attractivity matrix
            float vahaVzdalenost = 1.0f;
            float vahaFeromon = 2.0f;
            float vahaNahoda = 1.0f;
            propertySupport.firePropertyChange("progressMessage", null, "Ant "+(k+1)+" from "+(antsCount)+" calculating attraction");
            for (i=0; i<pocetBodu;i++) {
                for (j=i+1; j<pocetBodu;j++) {
                    if (distances[i][j] == 0) {
                        attract = 1;
                    } else if (distances[i][j] == -1) {
                        attract = 10;
                    } else if (k==(antsCount-1)) {
                        attract = (float) (Math.pow(1/distances[i][j],vahaVzdalenost) * Math.pow(pheromones[i][j],vahaFeromon));
                    } else {
                        attract = (float) (Math.pow(1/distances[i][j],vahaVzdalenost) * Math.pow(pheromones[i][j],vahaFeromon) * Math.pow(Math.random()*1.0 + 0.5,vahaNahoda));
                    }
                    attractivity[i][j] = attract;
                    attractivity[j][i] = attract;
                }
            propertySupport.firePropertyChange("progressValue", 0, (int) ((100.0*i)/pocetBodu));                            
            }
            // generate ant path
            numProcessed = 0;
            propertySupport.firePropertyChange("progressMessage", null, "Ant "+(k+1)+" from "+(antsCount)+" running through lines");
            while (numProcessed < pocetBodu) {
                maxAttractivity = 0;
                for(i=0; i<pocetBodu; i++) {
                    if (i!=lastPoint && !processed[i] && attractivity[lastPoint][i]>maxAttractivity) {
                        nextPoint = i;
                        maxAttractivity = attractivity[lastPoint][nextPoint];
                    }
                }
                processed[nextPoint] = true;
                antPath[numProcessed] = nextPoint;
                lastPoint = nextPoint;
                numProcessed++;
                propertySupport.firePropertyChange("progressValue", 0, (int) ((100.0*numProcessed)/pocetBodu));                            
            }
            // calculate path length
            antPathLength = 0.0;
            for (i=1; i<pocetBodu; i++) {
                antPathLength += distances[antPath[i]][antPath[i-1]];
            }
            System.out.println("ant path length = "+antPathLength);
            
            // update pheromones
            float pheromone;
            for (i=0; i<pocetBodu;i++) {
                for (j=i+1; j<pocetBodu;j++) {
                    pheromone = 0.9f*pheromones[i][j];
                    pheromones[i][j] = pheromone;
                    pheromones[j][i] = pheromone;
                }
            }
            System.out.println("pheromone increase = " + 20000.0f/antPathLength);
            int r,s;
            propertySupport.firePropertyChange("progressMessage", null, "Ant "+(k+1)+" from "+(antsCount)+" deposing pheromones");
            for (i=1; i<pocetBodu; i++) {
                r = antPath[i];
                s = antPath[i-1];
                pheromone = (float) (pheromones[r][s] + 20000.0f/antPathLength);
                pheromones[r][s] = pheromone;
                pheromones[s][r] = pheromone;
                propertySupport.firePropertyChange("progressValue", 0, (int) ((100.0*i)/pocetBodu));                            
            }
        }
        propertySupport.firePropertyChange("progressMessage", null, "Generating final path.");
        for (i=1; i<pocetBodu; i++) {
            for(j=0; j<pocetCar; j++) {
                if (lines_1[j] == antPath[i-1] && lines_2[j] == antPath[i]) {
                    p.lines_1[numLinesProcessed] = lines_1[j];
                    p.lines_2[numLinesProcessed] = lines_2[j];
                    p.pens[numLinesProcessed] = pens[j];
                    p.status[numLinesProcessed] = status[j];
                    linesProcessed[j] = true; 
                    numLinesProcessed++;
                    break;
                } else if (lines_2[j] == antPath[i-1] && lines_1[j] == antPath[i]) {
                    p.lines_1[numLinesProcessed] = lines_2[j];
                    p.lines_2[numLinesProcessed] = lines_1[j];
                    p.pens[numLinesProcessed] = pens[j];
                    p.status[numLinesProcessed] = status[j];
                    linesProcessed[j] = true;                    
                    numLinesProcessed++;
                    break;
                }         
            }
            propertySupport.firePropertyChange("progressValue", 0, (int) ((100.0*i)/pocetBodu));                            
        }
        p.calculatePathLengths();
        return p;
    }

    private PLTfile optimizationGreedy() {
        PLTfile p = (PLTfile) clone();
        calculateDistances();
        int numProcessed = 0;
        int numLinesProcessed = 0;
        int lastPoint = 0;
        int minPoint = 0;
        double minDist;
        boolean processed[] = new boolean[pocetBodu];
        processed[0] = true;
        boolean linesProcessed[] = new boolean[pocetCar];
        int i, j;
        while (numProcessed < pocetBodu) {
//            System.out.println("last point: "+lastPoint+"["+point_x[lastPoint]+","+point_y[lastPoint]+"]");
            minDist = 2*(max_x+max_y);
            for(i=0; i<pocetBodu; i++) {
                if (!processed[i] && distances[lastPoint][i]<minDist) {
                    minPoint = i;
                    minDist = distances[lastPoint][minPoint];
                }
            }
            numProcessed++;
            processed[minPoint] = true;
            for(j=0; j<pocetCar; j++) {
                if (lines_1[j] == lastPoint && lines_2[j] == minPoint) {
                    p.lines_1[numLinesProcessed] = lines_1[j];
                    p.lines_2[numLinesProcessed] = lines_2[j];
                    p.pens[numLinesProcessed] = pens[j];
                    p.status[numLinesProcessed] = status[j];
                    linesProcessed[j] = true; 
                    numLinesProcessed++;
                    break;
                }
                if (lines_2[j] == lastPoint && lines_1[j] == minPoint) {
                    p.lines_1[numLinesProcessed] = lines_2[j];
                    p.lines_2[numLinesProcessed] = lines_1[j];
                    p.pens[numLinesProcessed] = pens[j];
                    p.status[numLinesProcessed] = status[j];
                    linesProcessed[j] = true;                    
                    numLinesProcessed++;
                    break;
                }            
            }
//            System.out.println("processed lines: " + numLinesProcessed);
            lastPoint = minPoint;
        }
//        System.out.println("total lines = "+pocetCar + "; zpracovano = "+numLinesProcessed);
        p.calculatePathLengths();
        return p;
    }
    
    private void calculateDistances() {
        float dx, dy;
        float dist;
        distances = new float[pocetBodu][pocetBodu];
        int i, j, k;
        propertySupport.firePropertyChange("progressMessage", null, "distance calculation");
        for (i=0; i<pocetBodu; i++) {
            distances[i][i] = 2*(max_x+max_y);
            for (j=i+1; j<pocetBodu; j++) {
                dx = point_x[i] - point_x[j];
                dy = point_y[i] - point_y[j];
                dist = (float) Math.sqrt(dx*dx + dy*dy);
                distances[i][j] = dist;
                distances[j][i] = dist;
            }
            propertySupport.firePropertyChange("progressValue", 0, (int) ((100.0*i)/pocetBodu));            
        }
        // set distance of points, that are common for just two lines to bad value
        propertySupport.firePropertyChange("progressMessage", null, "loops detection");
        int zeroDistCount;
        for (i=0; i<pocetBodu; i++) {
            zeroDistCount = 0;
            for (j=0; j<pocetBodu; j++) {
                if (distances[i][j]==0) {
                    zeroDistCount++;    
                }
            }
            lines_at_point[i] = zeroDistCount+1;
        }
        for (i=0; i<pocetBodu; i++) {
            if (lines_at_point[i]==2) {
                k = -1;
                for (k=0; k<pocetCar; k++) {
                    if (lines_1[k]==i || lines_2[k]==i) {
                        break;
                    }
                }
//                if (i==1272) {
//                    System.out.println("is loop = "+findLoop(k)+"; k = "+k);
//                }
                if (!findLoop(k)) {
                    for (j=0; j<pocetBodu; j++) {
                        if (distances[i][j]!= 0) {
                            distances[i][j] = 2*(max_x+max_y);
                            distances[j][i] = 2*(max_x+max_y);
                        }
                    }
                }
            }
            propertySupport.firePropertyChange("progressValue", 0, (int) ((100.0*i)/pocetBodu));            
        }
        
        // set distance of all connected points best value
        for (i=0; i<pocetCar; i++) {
            distances[lines_1[i]][lines_2[i]] = -1.0f;
            distances[lines_2[i]][lines_1[i]] = -1.0f;
        }
        System.gc();
//        DecimalFormat format = new DecimalFormat("#####");
//        for (i=1260; i<1280; i++) {
//            for (j=1260; j<1280; j++) {
//                System.out.print(format.format(distances[i][j]) + "\t");
//            }
//            System.out.println();
//        }
        propertySupport.firePropertyChange("progressFinished", false, true);
    }
    
    private PLTfile optimizationGreedyModified() {
        PLTfile p = (PLTfile) clone();
        boolean processed[] = new boolean[pocetCar];
        int numProcessed = 0;
        int lastX2 = 0, lastY2 = 0, lastPen = -1;
        int range = 0;
        boolean canChangePen = false;
        int rangeNulling = 0;
        int vzdalenost1x, vzdalenost1y, vzdalenost2x, vzdalenost2y, vzdalenost1, vzdalenost2;
        while (numProcessed < pocetCar) {
//            System.out.println("processed="+numProcessed+"; range = "+range);
            boolean findLineStart = true;
            if (rangeNulling > 10) {
                findLineStart = false;
//                rangeNulling = 0;
            }
            boolean haveLine;
            do {
                haveLine = false;
                for (int j=0; j<pocetCar; j++) {
                    if (processed[j] || (lastPen != -1 && !canChangePen && pens[j] != lastPen)) {
//                    if (processed[j]) {
                        continue;
                    }
                    vzdalenost1x = Math.abs(point_x[lines_1[j]] - lastX2);
                    vzdalenost1y = Math.abs(point_y[lines_1[j]] - lastY2);
                    vzdalenost2x = Math.abs(point_x[lines_2[j]] - lastX2);
                    vzdalenost2y = Math.abs(point_y[lines_2[j]] - lastY2);
                    vzdalenost1 = (int) Math.sqrt(vzdalenost1x*vzdalenost1x + vzdalenost1y*vzdalenost1y);
                    vzdalenost2 = (int) Math.sqrt(vzdalenost2x*vzdalenost2x + vzdalenost2y*vzdalenost2y);
//                    if ((Math.sqrt(1.0*Math.pow(x1[j] - lastX2,2) + Math.pow(y1[j] - lastY2,2)) <= range)
//                            && (!findLineStart || (status[j]!=2 && status[j]!=4))) {
//                    if ((Math.abs(x1[j] - lastX2) <= range) && (Math.abs(y1[j] - lastY2) <= range)) {
                    if (vzdalenost1==0 || vzdalenost2==0 || ((vzdalenost1 <= range) && (vzdalenost2 <= range)
                            && (!findLineStart || status[j]!=4))) {
                        
                        if (vzdalenost1==0 || (vzdalenost2!=0 && ((status[j]==1) || (vzdalenost1<=vzdalenost2 && status[j]!=2)))) {
                            p.lines_1[numProcessed] = lines_1[j];
                            p.lines_2[numProcessed] = lines_2[j];
                        } else {
                            p.lines_1[numProcessed] = lines_2[j];
                            p.lines_2[numProcessed] = lines_1[j];
                        }
                        lastX2 = point_x[p.lines_2[numProcessed]];
                        lastY2 = point_y[p.lines_2[numProcessed]];
                        
                        p.pens[numProcessed] = pens[j];
                        p.status[numProcessed] = status[j];
                        lastPen = pens[j];
                        canChangePen = false;
                        processed[j] = true;
                        numProcessed++;
                        haveLine = true;
                        findLineStart = false;
                        range = 0;
                        break;
                    }
                }
                //System.out.println("last processed = "+numProcessed);
            } while (haveLine);
            
            if (range == 0) {
                range = 2;
                //range = max_x + max_y;
            } else if(range > (max_x+max_y)) {
                canChangePen = true;
                range = 0;
                lastX2 = 0;
                lastY2 = 0;
                rangeNulling++;
            } else {
                range = (int) Math.round(range*1.3);
                //range = max_x + max_y;
            }
        }
        
        p.calculatePathLengths();
        return p;
    }

    private void calculatePathLengths() {
        int lastPoint = 0;
        int lastX2 = 0;
        int lastY2 = 0;
        delkaCar = 0;
        pocetPrejezdu = 0;
        delkaPrejezdu = 0;
        for (int i=0; i<pocetCar; i++) {
            delkaCar += Math.sqrt(Math.pow(point_x[lines_2[i]] - point_x[lines_1[i]],2) + Math.pow(point_y[lines_2[i]]-point_y[lines_1[i]],2));
            if (point_x[lines_1[i]] != point_x[lastPoint] || point_y[lines_1[i]] != point_y[lastPoint]) {
                pocetPrejezdu++;
                delkaPrejezdu += Math.sqrt(Math.pow(point_x[lines_1[i]]-lastX2,2) + Math.pow(point_y[lines_1[i]]-lastY2,2));
            }
            lastPoint = lines_2[i];
            lastX2 = point_x[lastPoint];
            lastY2 = point_y[lastPoint];
        }        
    }
    
    public void saveToFile(File file) {
        PrintWriter bw = null;
        try {
            bw = new PrintWriter(new FileWriter(file));
            bw.write("IN;SC;PU;RO0;IP;IW;VS15");
            int lastX = -1, lastY = -1, lastPen = -1;
            for(int i=0; i<pocetCar; i++) {
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

    public int[] getPens() {
        return pens;
    }

    public double getDelkaCar() {
        return delkaCar;
    }

    public double getDelkaPrejezdu() {
        return delkaPrejezdu;
    }

    public int getPocetCar() {
        return pocetCar;
    }

    public int getPocetPrejezdu() {
        return pocetPrejezdu;
    }

    public int getMax_x() {
        return max_x;
    }

    public int getMax_y() {
        return max_y;
    }

    public int[] getStatus() {
        return status;
    }

    public int[] getLines_1() {
        return lines_1;
    }

    public int[] getLines_2() {
        return lines_2;
    }

    public int[] getPoint_x() {
        return point_x;
    }

    public int[] getPoint_y() {
        return point_y;
    }

    @Override
    protected Object clone() {
        PLTfile p = new PLTfile();
        p.rawPLT = rawPLT;
        p.point_x = new int[2*pocetCar+1];
        p.point_y = new int[2*pocetCar+1];
        p.lines_at_point = new int[2*pocetCar+1];
        for (int i=0; i<2*pocetCar+1; i++) {
            p.point_x[i] = point_x[i];
            p.point_y[i] = point_y[i];
            p.lines_at_point[i] = lines_at_point[i];
        }
        p.pocetCar = pocetCar;
        p.pocetBodu = pocetBodu;
        p.max_x = max_x;
        p.max_y = max_y;
        p.pens = new int[pocetCar];
        p.lines_1 = new int[pocetCar];
        p.lines_2 = new int[pocetCar];
        p.status = new int[pocetCar];
        
        return p;
    }
        
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public long getRequiredMemory() {
        switch(alghoritm) {
            case 3:
                return 2*4*10*pocetCar + pocetCar/8 + pocetBodu/8 + 4*pocetBodu + 2*4*pocetBodu*pocetBodu + 50*1024*1024;
            case 2:
                return 2*4*10*pocetCar + 2*4*pocetBodu + 50*1024*1024;
            case 1:
            default:
                return 2*4*10*pocetCar + 2*4*pocetBodu + 50*1024*1024;
        }        
    }
    
}
