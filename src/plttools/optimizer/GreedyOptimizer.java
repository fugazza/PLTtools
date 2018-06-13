/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package plttools.optimizer;

import plttools.ChildrenProgressCalculator;
import plttools.PLTdata;
import plttools.ParentProgressCalculator;

/**
 *
 * @author vlada
 */
public class GreedyOptimizer extends AbstractOptimizer {
    private final ChildrenProgressCalculator optimizationProgress = new ChildrenProgressCalculator();
    private GreedyOptimizer[] subPlotOptimizers;

    @Override
    public PLTdata optimize() {
        progress.setMessage("greedy optimization");
        PLTdata p = new PLTdata();
        p.setLineCount(pd.getPopulatedLines());
        p.setPen(pd.getPen()); 
        // optimize all subplots first
        for (GreedyOptimizer optimizer: subPlotOptimizers) {
            System.out.println("optimization of subplot start");
            PLTdata optimizedSubPlot;
            optimizedSubPlot = optimizer.optimize();
            optimizedSubPlot.calculateStats();
            p.addSubPlot(optimizedSubPlot);
        }
        
        pd.calculateDistances();
        int numLinesProcessed = 0;
        int numPointsProcessed = 0;
        int lastPoint = 0;
        int preLastPoint = 0;
        int minPoint = 0;
        double minDist;
        int i, j;
        int numEvaluatedPoints = 2*pd.getPopulatedLines();
        int pocetBodu = pd.getPointsCount();
        byte passThroughPointLeft[] = new byte[pocetBodu];
        for (i=0; i<pocetBodu; i++) {
            passThroughPointLeft[i] = pd.getLinesAtPoint(i);
        }
        
        // find first point which is closest to [0,0] and is not part of multiline
        // last point means first point for next part of alghoritm
        minDist = (pd.getBoundingBox().getMaxX()+pd.getBoundingBox().getMaxY());
        System.out.println("searching first point for pen " + pd.getPen() + "; minDist = " + minDist);
        double dist;
        for(i = 0; i<pd.getPointsCount(); i++) {
            dist = Math.sqrt(Math.pow(pd.getPoint_x()[i],2) + Math.pow(pd.getPoint_y()[i],2));
            if (dist < minDist && (pd.getStatusAtPoint(i) == 1 || pd.getStatusAtPoint(i) == 3)) {
                lastPoint = i;
                minDist = dist;
            }
        }
        numPointsProcessed = 1;
        System.out.println("first point for pen " + pd.getPen() + " is #" + lastPoint + " ["+pd.getPoint_x()[lastPoint]+";"+pd.getPoint_y()[lastPoint]+"]");
        
        // connect all other points        
        while (numPointsProcessed < numEvaluatedPoints && numLinesProcessed < pd.getPopulatedLines()) {
            optimizationProgress.setProgress(numPointsProcessed);
            System.out.println("last point: "+lastPoint+"["+pd.getPoint_x()[lastPoint]+","+pd.getPoint_y()[lastPoint]+"]");
            minDist = 2*(pd.getBoundingBox().getWidth()+pd.getBoundingBox().getHeight());
            for(i=0; i<pd.getPointsCount(); i++) {
                if ((i != preLastPoint) && (passThroughPointLeft[i]>0) && (pd.isLineBetween(lastPoint, i) || pd.getDistance(lastPoint,i)<minDist)) {
                    minPoint = i;
                    minDist = pd.getDistance(lastPoint,minPoint);
                    if (pd.isLineBetween(lastPoint, minPoint)) {
                        passThroughPointLeft[lastPoint]--;
                        passThroughPointLeft[minPoint]--;
                        break;
                    }
                }
            }

            numPointsProcessed++;
            int l1, l2, x1, y1, x2, y2;
            for(j=0; j<pd.getPopulatedLines(); j++) {
                l1 = pd.getLines_1()[j];
                l2 = pd.getLines_2()[j];
                x1 = pd.getPoint_x()[l1];
                y1 = pd.getPoint_y()[l1];
                x2 = pd.getPoint_x()[l2];
                y2 = pd.getPoint_y()[l2];
                if (l1 == lastPoint && l2 == minPoint) {
                    numLinesProcessed++;
                    p.addLine(x1, y1, x2, y2);
                    break;
                }
                if (l2 == lastPoint && l1 == minPoint) {
                    numLinesProcessed++;
                    p.addLine(x2, y2, x1, y1);
                    break;
                }            
            }
            System.out.println("processed lines: " + numLinesProcessed);
            preLastPoint = lastPoint;
            lastPoint = minPoint;
        }
        System.out.println("total lines = "+pd.getPopulatedLines() + "; num of processed lines = "+numLinesProcessed+ "; num of processed points = "+numPointsProcessed);
        p.calculateStats();
        progress.finishedAnnouncement();
        return p;
    }
   
    @Override
    public boolean changesLineCount() {
        return false;
    }

    @Override
    public Object clone() {
        GreedyOptimizer clon;
        try {
            clon = (GreedyOptimizer) super.clone();
        } catch (CloneNotSupportedException ex) {
            clon = new GreedyOptimizer();
        }
        clon.settings = this.settings;
        return clon;
    }     

    @Override
    protected void prepareProgressCalculators() {
        if (pd != null) {
            optimizationProgress.setMax(2*pd.getPopulatedLines());
            subPlotOptimizers = new GreedyOptimizer[pd.getSubPlotsCount()];
            int ii=0;
            for (PLTdata subPlot: pd.getSubPlots()) {
                GreedyOptimizer optimizer = (GreedyOptimizer) this.clone();
                optimizer.setData(subPlot);
                subPlotOptimizers[ii++] = optimizer;  
                ParentProgressCalculator progCalc = new ParentProgressCalculator();
                progress.register(progCalc);
                optimizer.setProgressCalculator(progCalc);
            }
        } else {
            optimizationProgress.setMax(100);
        }
        progress.register(optimizationProgress);    }
}
