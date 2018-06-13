/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package plttools.optimizer;

import java.text.DecimalFormat;
import plttools.ChildrenProgressCalculator;
import plttools.PLTdata;
import plttools.ParentProgressCalculator;

/**
 *
 * @author vlada
 */
public class AntColonyOptimizer extends AbstractOptimizer {

    private ParentProgressCalculator[] optimizationProgress;
    private ChildrenProgressCalculator[] attractivityProgress;
    private ChildrenProgressCalculator[] antRunProgress;
    private ChildrenProgressCalculator[] pheromonesProgress;
    private ChildrenProgressCalculator finalPathProgress;
    private AntColonyOptimizer[] subPlotOptimizers;

    private float pheromones[][];
            
    @Override
    public PLTdata optimize() {
//        System.out.println("optimization started");
        progress.setMessage("ant colony optimization started");
//        System.out.println("property fired");
        PLTdata p = new PLTdata();
        p.setPen(pd.getPen());
        // optimize all subplots first
        for (AntColonyOptimizer optimizer: subPlotOptimizers) {
            System.out.println("optimization of subplot start");
            PLTdata optimizedSubPlot;
            optimizedSubPlot = optimizer.optimize();
            optimizedSubPlot.calculateStats();
            p.addSubPlot(optimizedSubPlot);
        }
        
        pd.calculateDistances();
//        System.out.println("distances calculated");
        int numProcessed;
        double maxAttractivity;
        int pocetBodu = pd.getPointsCount();
        int antPath[] = new int[2*pd.getLinesCount()];
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
        int numEvaluatedPoints = 2*pd.getLinesCount();
        int antsCount = settings.getAntCount();
        for (k=0; k<antsCount; k++) {
            attractivityProgress[k].setMessage("Ant "+(k+1)+" from "+(antsCount)+"is running");
            // generate attractivity matrix
            attractivityProgress[k].setMessage("Ant "+(k+1)+" from "+(antsCount)+" calculating attraction");
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
                attractivityProgress[k].setProgress(i);
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
            antRunProgress[k].setMessage("Ant "+(k+1)+" from "+(antsCount)+" running through lines");
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
                antRunProgress[k].setProgress(numProcessed);
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
            double pheromoneUnit = (pd.getTravelsLength() / antPathLength )-1;
            System.out.println("pheromone increase = " + pheromoneUnit);
            int r,s;
            pheromonesProgress[k].setMessage("Ant "+(k+1)+" from "+(antsCount)+" deposing pheromones");
            for (i=1; i<pocetBodu; i++) {
                r = antPath[i];
                s = antPath[i-1];
                pheromone = (float) (pheromones[r][s] + pheromoneUnit);
                pheromones[r][s] = pheromone;
                pheromones[s][r] = pheromone;
                pheromonesProgress[k].setProgress(i);
            }
        }
        finalPathProgress.setMessage("Generating final path.");
        p.setLineCount(pd.getLinesCount());
        Boolean linesFound[] = new Boolean[pd.getLinesCount()];
        for (i=0; i<pd.getLinesCount(); i++) {
            linesFound[i] = false;
        }
        int l1, l2, x1, y1, x2, y2;
        for (i=1; i<numEvaluatedPoints; i++) {
            for(j=0; j<pd.getLinesCount(); j++) {
                l1 = pd.getLines_1()[j];
                l2 = pd.getLines_2()[j];
                x1 = pd.getPoint_x()[l1];
                y1 = pd.getPoint_y()[l1];
                x2 = pd.getPoint_x()[l2];
                y2 = pd.getPoint_y()[l2];
                if (l1 == antPath[i-1] && l2 == antPath[i]) {
                    p.addLine(x1, y1, x2, y2);
//                    System.out.println("added line from [" + x1 + ";" + y1 + "] to [" + x2 + ";" + y2 + "]");
                    linesFound[j] = true;
                    break;
                } else if (l2 == antPath[i-1] && l1 == antPath[i]) {
                    p.addLine(x2, y2, x1, y1);
//                    System.out.println("added line from [" + x2 + ";" + y2 + "] to [" + x1 + ";" + y1 + "]");
                    linesFound[j] = true;
                    break;
                }  
            }
            finalPathProgress.setProgress(i);
        }
        
        for (i=0; i<pd.getLinesCount(); i++) {
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
        p.calculateStats();
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

    @Override
    public boolean changesLineCount() {
        return false;
    }

    @Override
    public Object clone() {
        AntColonyOptimizer clon;
        try {
            clon = (AntColonyOptimizer) super.clone();
        } catch (CloneNotSupportedException ex) {
            clon = new AntColonyOptimizer();
        }
        clon.settings = this.settings;
        return clon;
    }     

    @Override
    protected void prepareProgressCalculators() {
        optimizationProgress = new ParentProgressCalculator[settings.getAntCount()];
        attractivityProgress = new ChildrenProgressCalculator[settings.getAntCount()];
        antRunProgress = new ChildrenProgressCalculator[settings.getAntCount()];
        pheromonesProgress = new ChildrenProgressCalculator[settings.getAntCount()];

        if (pd != null) {
            subPlotOptimizers = new AntColonyOptimizer[pd.getSubPlotsCount()];
            int ii=0;
            for (PLTdata subPlot: pd.getSubPlots()) {
                ParentProgressCalculator subPlotProgressCalculator = new ParentProgressCalculator();
                AntColonyOptimizer optimizer = (AntColonyOptimizer) this.clone();
                optimizer.setData(subPlot);
                subPlotOptimizers[ii++] = optimizer;  
                progress.register(subPlotProgressCalculator);
                optimizer.setProgressCalculator(subPlotProgressCalculator);

                for (int i=0; i<optimizationProgress.length; i++) {
                    optimizationProgress[i] = new ParentProgressCalculator();
                    progress.register(optimizationProgress[i]);        
                    attractivityProgress[i] = new ChildrenProgressCalculator();
                    attractivityProgress[i].setMax(pd.getPointsCount());
                    optimizationProgress[i].register(attractivityProgress[i]);
                    antRunProgress[i] = new ChildrenProgressCalculator();
                    antRunProgress[i].setMax(pd.getPointsCount());
                    optimizationProgress[i].register(antRunProgress[i]);
                    pheromonesProgress[i] = new ChildrenProgressCalculator();
                    pheromonesProgress[i].setMax(pd.getPointsCount());
                    optimizationProgress[i].register(pheromonesProgress[i]);
                    
                }
                finalPathProgress = new ChildrenProgressCalculator();
                finalPathProgress.setMax(pd.getPointsCount());
                progress.register(finalPathProgress);
            }
        } else {
            for (ParentProgressCalculator pc: optimizationProgress) {
                ParentProgressCalculator optProg = new ParentProgressCalculator();
                ChildrenProgressCalculator childProg = new ChildrenProgressCalculator();
                childProg.setMax(100);
                optProg.register(childProg);
                pc = optProg;
                progress.register(pc);        
            }
        }
    }
}
