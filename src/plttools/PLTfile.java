/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package plttools;

import java.io.*;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vláďa
 */
public class PLTfile {

    private StringBuilder rawPLT;
    private int max_x = 0;
    private int max_y = 0;
    private int last_x = 0;
    private int last_y = 0;
    private int pens[], x1[], y1[], x2[], y2[], status[];
    private int pocetCar = 0;
    private int pocetPrejezdu = 0;
    private double delkaCar = 0.0;
    private double delkaPrejezdu = 0.0;
    private int pen = 0;
    
    public static PLTfile readPLTfromFile(File file) {
        BufferedReader reader = null;
        PLTfile plt = new PLTfile();
        try {
            reader = new BufferedReader(new FileReader(file));
            StringBuilder contents = new StringBuilder();
            String text = "";
            while ((text = reader.readLine()) != null) {
                contents.append(text).append(System.getProperty("line.separator"));            
            }
            plt.setRawPLT(contents);
            plt.parseRaw();
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
        return plt;
    }

    private void parseRaw() {
        String prikaz;
        boolean penUp = true;
        // spočítat čáry a přejezdy
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
        
        
        // teď teprve získat jednotlivé hodnoty
        int x2a, y2a, i = 0;
        pens = new int[pocetCar];
        x1 = new int[pocetCar];
        y1 = new int[pocetCar];
        x2 = new int[pocetCar];
        y2 = new int[pocetCar];
        status = new int[pocetCar];
        
        penUp = true;
        StringTokenizer st = new StringTokenizer(rawPLT.toString(),";");
        while (st.hasMoreTokens()) {
            prikaz = st.nextToken();

            if (prikaz.startsWith("PUPA") || (penUp && prikaz.startsWith("PA"))) {
                int commandLength = (prikaz.startsWith("PUPA")) ? 4 : 2;
                StringTokenizer st2 = new StringTokenizer(prikaz.substring(commandLength),",");
                while (st2.hasMoreTokens()) {
                    x2a = Integer.parseInt(st2.nextToken());
                    y2a = Integer.parseInt(st2.nextToken());

                    delkaPrejezdu += Math.sqrt(Math.pow(x2a - last_x, 2) + Math.pow(y2a - last_y, 2));
                    if (x2a > max_x) {
                        max_x = x2a;
                    }
                    if (y2a > max_y) {
                        max_y = y2a;
                    }
                    last_x = x2a;
                    last_y = y2a;
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
                    
                    pens[i] = pen;
                    x1[i] = last_x;
                    y1[i] = last_y;
                    x2[i] = x2a;
                    y2[i] = y2a;
                    delkaCar += Math.sqrt(Math.pow(x2a - last_x, 2) + Math.pow(y2a - last_y, 2));
                    if (x2a > max_x) {
                        max_x = x2a;
                    }
                    if (y2a > max_y) {
                        max_y = y2a;
                    }
                    last_x = x2a;
                    last_y = y2a;
                    i++;
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
        
        for (int k=0; k < pocetCar; k++) {
            if (status[k] <= 0) {
                status[k] = checkStatus(k);
                //pens[k] = status[k];
            }
//            System.out.println("status of " + k + " = "+status[k]+"; pen = " +pens[k]);
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
        if (status[index] > 0) {
            if (status[index]==4) {
                status[index]=3;
            }
            return status[index];
        }
        int countOn1 = 0, countOn2 = 0;
        int indexOn1 = -1, indexOn2 = -1;
        for (int k=0; k < pocetCar; k++) {
            if (k==index) {
                continue;
            } else {
                if (x1[k] == x1[index] && y1[k] == y1[index]) {
                    countOn1++;
                    indexOn1 = k;
                } else if (x2[k] == x1[index] && y2[k] == y1[index]) {                
                    countOn1++;
                    indexOn1 = k;
                }            
                if (x1[k] == x2[index] && y1[k] == y2[index]) {
                    countOn2++;
                    indexOn2 = k;
                } else if (x2[k] == x2[index] && y2[k] == y2[index]) {                
                    countOn2++;
                    indexOn2 = k;
                }      
            }
        }        
        if (countOn1==1 && countOn2==1) {
            if (findLoop(x1[index],y1[index],index,index)) {
                return 3;
            } else {
                return 4;                        
            }
        }
        if (countOn1!=1 && countOn2!=1) {
            return 5;
        }
        if (countOn1==1 && countOn2!=1) {
            return 2;
        }
        if (countOn1!=1 && countOn2==1) {
            return 1;
        }
        return 6;
    }

    
    private boolean findLoop(int x, int y, int k, int startIndex) {
        int x2a = x, y2a = y, ka = k;
        int tempX = 0, tempY = 0, tempK = 0;
        boolean hledatDalsi = true;
        while (hledatDalsi) {
            int nalezeno = -1;
            hledatDalsi = false;
            boolean nalezenoVice = false;
            for (int i=0; i<pocetCar; i++) {
                if (i!=ka) {
                    if (x1[i]==x2a && y1[i]==y2a) {
                        if (nalezeno!=-1) {
                            nalezenoVice = true;
                            hledatDalsi = false;
                            break;
                        } else {
                            nalezeno = i;
                            tempX = x2[i];
                            tempY = y2[i];
                            tempK = i;
                            hledatDalsi = true;
                        }
                    }
                    if (x2[i]==x2a && y2[i]==y2a) {
                        if (nalezeno!=-1) {
                            nalezenoVice = true;
                            hledatDalsi = false;
                            break;
                        } else {
                            nalezeno = i;
                            tempX = x1[i];
                            tempY = y1[i];
                            tempK = i;
                            hledatDalsi = true;
                        }
                    }
                }
            }
            if (hledatDalsi) {
                x2a = tempX;
                y2a = tempY;
                ka = tempK;                
            }
            //System.out.println("nalezeno = " + nalezeno + "; startIndex = "+startIndex);
            if (!nalezenoVice && (nalezeno == startIndex || (nalezeno!= -1 && status[nalezeno] == 3))) {
                return true;
            }
        }
        return false;
    } 
    
    public PLTfile getOptimizedPLT() {
        PLTfile p = new PLTfile();
        p.pocetCar = pocetCar;
        p.pocetPrejezdu = pocetPrejezdu;
        p.max_x = max_x;
        p.max_y = max_y;
        p.pens = new int[pocetCar];
        p.x1 = new int[pocetCar];
        p.y1 = new int[pocetCar];
        p.x2 = new int[pocetCar];
        p.y2 = new int[pocetCar];
        p.status = new int[pocetCar];
        boolean processed[] = new boolean[pocetCar];
        p.rawPLT = rawPLT;
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
                    vzdalenost1x = Math.abs(x1[j] - lastX2);
                    vzdalenost1y = Math.abs(y1[j] - lastY2);
                    vzdalenost2x = Math.abs(x2[j] - lastX2);
                    vzdalenost2y = Math.abs(y2[j] - lastY2);
                    vzdalenost1 = (int) Math.sqrt(vzdalenost1x*vzdalenost1x + vzdalenost1y*vzdalenost1y);
                    vzdalenost2 = (int) Math.sqrt(vzdalenost2x*vzdalenost2x + vzdalenost2y*vzdalenost2y);
//                    if ((Math.sqrt(1.0*Math.pow(x1[j] - lastX2,2) + Math.pow(y1[j] - lastY2,2)) <= range)
//                            && (!findLineStart || (status[j]!=2 && status[j]!=4))) {
//                    if ((Math.abs(x1[j] - lastX2) <= range) && (Math.abs(y1[j] - lastY2) <= range)) {
                    if (vzdalenost1==0 || vzdalenost2==0 || ((vzdalenost1 <= range) && (vzdalenost2 <= range)
                            && (!findLineStart || status[j]!=4))) {
                        
                        if (vzdalenost1==0 || (vzdalenost2!=0 && ((status[j]==1) || (vzdalenost1<=vzdalenost2 && status[j]!=2)))) {
                            p.x1[numProcessed] = x1[j];
                            p.y1[numProcessed] = y1[j];
                            p.x2[numProcessed] = x2[j];
                            p.y2[numProcessed] = y2[j];                            
                        } else {
                            p.x1[numProcessed] = x2[j];
                            p.y1[numProcessed] = y2[j];
                            p.x2[numProcessed] = x1[j];
                            p.y2[numProcessed] = y1[j];
                        }
                        lastX2 = p.x2[numProcessed];
                        lastY2 = p.y2[numProcessed];                            
                        
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
        
        if (p.x1[0] !=0 || p.y1[0] != 0) {
            p.pocetPrejezdu = 1;
            p.delkaPrejezdu = Math.sqrt(Math.pow(p.x1[0],2) + Math.pow(p.y1[0],2));
        } else {
            p.pocetPrejezdu = 0;
        }
        p.delkaCar = Math.sqrt(Math.pow(p.x2[0] - p.x1[0],2) + Math.pow(p.y2[0]-p.y1[0],2));
        for (int i=1; i<pocetCar; i++) {
            p.delkaCar += Math.sqrt(Math.pow(p.x2[i] - p.x1[i],2) + Math.pow(p.y2[i]-p.y1[i],2));
            if (p.x1[i] != p.x2[i-1] || p.y1[i] != p.y2[i-1]) {
                p.pocetPrejezdu++;
                p.delkaPrejezdu += Math.sqrt(Math.pow(p.x1[i]-p.x2[i-1],2) + Math.pow(p.y1[i]-p.y2[i-1],2));
            }
        }
        return p;
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
                if (x1[i]!=lastX || y1[i]!=lastY) {
                    bw.write(";PUPA"+x1[i]+","+y1[i]+";PDPA"+x2[i]+","+y2[i]);
                } else {
                    bw.write(","+x2[i]+","+y2[i]);
                }          
                lastX = x2[i];
                lastY = y2[i];
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

    public int[] getX1() {
        return x1;
    }

    public int[] getY1() {
        return y1;
    }

    public int[] getX2() {
        return x2;
    }

    public int[] getY2() {
        return y2;
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
        
}
