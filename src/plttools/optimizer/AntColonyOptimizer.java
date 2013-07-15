/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package plttools.optimizer;

import java.text.DecimalFormat;
import plttools.PLTdata;

/**
 *
 * @author vlada
 */
public class AntColonyOptimizer extends AbstractOptimizer {

    private float pheromones[][];
            
    @Override
    public PLTdata optimize() {
//        System.out.println("optimization started");
        propertySupport.firePropertyChange("progressMessage", null, "optimization started");
//        System.out.println("property fired");
        PLTdata p = new PLTdata();
        pd.calculateDistances();
//        System.out.println("distances calculated");
        int numProcessed;
        double maxAttractivity;
        int pocetBodu = pd.getPocetBodu();
        int antPath[] = new int[2*pd.getPocetCar()];
        double antPathLength;
        float attractivity[][] = new float[pocetBodu][pocetBodu];
        System.out.println("attractivity array allocated");
        pheromones = new float[pocetBodu][pocetBodu];
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
        int numEvaluatedPoints = 2*pd.getPocetCar();
        int antsCount = settings.getAntCount();
        for (k=0; k<antsCount; k++) {
            propertySupport.firePropertyChange("progressMessage", null, "Ant "+(k+1)+" from "+(antsCount)+"is running");
            // generate attractivity matrix
            propertySupport.firePropertyChange("progressMessage", null, "Ant "+(k+1)+" from "+(antsCount)+" calculating attraction");
//            DecimalFormat format = new DecimalFormat("#####.##");
//            System.out.println("Attractivity:");
            for (i=0; i<pocetBodu;i++) {
                for (j=0; j<i;j++) {
                    if (k==(antsCount-1)) {
                        attract = calculateAttractivity(i,j,false);
                    } else {
                        attract = calculateAttractivity(i,j,true);
                    }
                    attractivity[i][j] = attract;
                    attractivity[j][i] = attract;
//                    System.out.print(format.format(attractivity[i][j]) + "\t");
                }
//                System.out.println();
                propertySupport.firePropertyChange("progressValue", 0, (int) ((100.0*i)/pocetBodu));                            
            }

            // generate ant path
            int nextPoint = 0;
            byte passThroughPointLeft[] = new byte[pocetBodu];
            for (i=0; i<pocetBodu; i++) {
                passThroughPointLeft[i] = pd.getLinesAtPoint(i);
            }
            if (k==(antsCount-1)) {
                lastPoint = 0;
            } else {
                lastPoint = (int) (Math.random()*pocetBodu);
            }
            System.out.println("Ant "+k+"; startpoint = "+lastPoint + " ["+pd.getPoint_x()[lastPoint]+";"+pd.getPoint_y()[lastPoint]+"]");
            antPath[0] = lastPoint;
            numProcessed = 1;
            propertySupport.firePropertyChange("progressMessage", null, "Ant "+(k+1)+" from "+(antsCount)+" running through lines");
            while (numProcessed < numEvaluatedPoints) {
                maxAttractivity = 0.0;
                for(i=0; i<pocetBodu; i++) {
                    if ((i!=lastPoint) && (passThroughPointLeft[i]>0) && (attractivity[lastPoint][i]>maxAttractivity)) {
                        nextPoint = i;
                        maxAttractivity = attractivity[lastPoint][nextPoint];
                    }
                }
//                System.out.print(" ["+pd.getPoint_x()[nextPoint]+";"+pd.getPoint_y()[nextPoint]+"]");
                if (pd.isLineBetween(lastPoint,nextPoint)) {
                    passThroughPointLeft[lastPoint]--;
                    passThroughPointLeft[nextPoint]--;
                }
                antPath[numProcessed] = nextPoint;
                numProcessed++;
                // modify attractivity so that the ant does not return back
                attractivity[lastPoint][nextPoint] = 0;
                attractivity[nextPoint][lastPoint] = 0;
                lastPoint = nextPoint;
                propertySupport.firePropertyChange("progressValue", 0, (int) ((100.0*numProcessed)/pocetBodu));                            
            }
//            System.out.println();
//            System.out.println("ant path runs throuhg "+numProcessed+" points");
            // calculate path length
            antPathLength = 0.0;
            for (i=1; i<numEvaluatedPoints; i++) {
                if (! pd.isLineBetween(antPath[i],antPath[i-1])) {
                    antPathLength += pd.calculateDistance(antPath[i],antPath[i-1]);
                }
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
            double pheromoneUnit = (pd.getDelkaPrejezdu() / antPathLength )-1;
            System.out.println("pheromone increase = " + pheromoneUnit);
            int r,s;
            propertySupport.firePropertyChange("progressMessage", null, "Ant "+(k+1)+" from "+(antsCount)+" deposing pheromones");
            for (i=1; i<pocetBodu; i++) {
                r = antPath[i];
                s = antPath[i-1];
                pheromone = (float) (pheromones[r][s] + pheromoneUnit);
                pheromones[r][s] = pheromone;
                pheromones[s][r] = pheromone;
                propertySupport.firePropertyChange("progressValue", 0, (int) ((100.0*i)/pocetBodu));                            
            }
        }
        propertySupport.firePropertyChange("progressMessage", null, "Generating final path.");
        p.setLineCount(pd.getPocetCar());
        Boolean linesFound[] = new Boolean[pd.getPocetCar()];
        for (i=0; i<pd.getPocetCar(); i++) {
            linesFound[i] = false;
        }
        int l1, l2, x1, y1, x2, y2;
        for (i=1; i<numEvaluatedPoints; i++) {
            for(j=0; j<pd.getPocetCar(); j++) {
                l1 = pd.getLines_1()[j];
                l2 = pd.getLines_2()[j];
                x1 = pd.getPoint_x()[l1];
                y1 = pd.getPoint_y()[l1];
                x2 = pd.getPoint_x()[l2];
                y2 = pd.getPoint_y()[l2];
                if (l1 == antPath[i-1] && l2 == antPath[i]) {
                    p.addLine(x1, y1, x2, y2, pd.getPens()[j]);
//                    System.out.println("added line from [" + x1 + ";" + y1 + "] to [" + x2 + ";" + y2 + "]");
                    linesFound[j] = true;
                    break;
                } else if (l2 == antPath[i-1] && l1 == antPath[i]) {
                    p.addLine(x2, y2, x1, y1, pd.getPens()[j]);
//                    System.out.println("added line from [" + x2 + ";" + y2 + "] to [" + x1 + ";" + y1 + "]");
                    linesFound[j] = true;
                    break;
                }  
            }
            propertySupport.firePropertyChange("progressValue", 0, (int) ((100.0*i)/pocetBodu));                            
        }
        
        for (i=0; i<pd.getPocetCar(); i++) {
            if (!linesFound[i]) {
                l1 = pd.getLines_1()[i];
                l2 = pd.getLines_2()[i];
                x1 = pd.getPoint_x()[l1];
                y1 = pd.getPoint_y()[l1];
                x2 = pd.getPoint_x()[l2];
                y2 = pd.getPoint_y()[l2];
                //System.out.println("line " + i + " not found; between points ["+x1 + ";"+y1+"],["+x2 + ";"+y2+"]");
            }
        }
        p.calculatePathLengths();
//        System.out.println("original lines count = " + pd.getPopulatedLines() + "; optimized lines count = " +p.getPopulatedLines() );
//        System.out.println("original points count = " + pd.getPocetBodu() + "; optimized points count = " +p.getPocetBodu() );
        return p;
    }
    
    private float calculateAttractivity(int point_i, int point_j, boolean randomize) {
        float vahaVzdalenost = 0.2f;
        float vahaFeromon = 1.0f;
        float vahaNahoda = 1.0f;
        if (pd.isLineBetween(point_i, point_j)) {
            return 10;
        } else if (randomize) {
            return (float) (Math.pow(1/pd.getDistance(point_i, point_j),vahaVzdalenost) * Math.pow(pheromones[point_i][point_j],vahaFeromon) * Math.pow(Math.random()*1.0 + 0.5,vahaNahoda));
        } else {
            return (float) (Math.pow(1/pd.getDistance(point_i, point_j),vahaVzdalenost) * Math.pow(pheromones[point_i][point_j],vahaFeromon));
        }
//        return (float) (Math.pow(1/distance,vahaVzdalenost));
    }
}
