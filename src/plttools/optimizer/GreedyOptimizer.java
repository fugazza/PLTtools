/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package plttools.optimizer;

import plttools.PLTdata;

/**
 *
 * @author vlada
 */
public class GreedyOptimizer extends AbstractOptimizer {

    @Override
    public PLTdata optimize() {
        PLTdata p = new PLTdata();
        pd.calculateDistances();
        int numProcessed = 0;
        int lastPoint = 0;
        int preLastPoint = 0;
        int minPoint = 0;
        double minDist;
        int i, j;
        p.setLineCount(pd.getPocetCar());
        int numEvaluatedPoints = 2*pd.getPocetCar();
        int pocetBodu = pd.getPocetBodu();
        byte passThroughPointLeft[] = new byte[pocetBodu];
        for (i=0; i<pocetBodu; i++) {
            passThroughPointLeft[i] = pd.getLinesAtPoint(i);
        }
        
        while (numProcessed < numEvaluatedPoints) {
            System.out.println("last point: "+lastPoint+"["+pd.getPoint_x()[lastPoint]+","+pd.getPoint_y()[lastPoint]+"]");
            minDist = 2*(pd.getBoundingBox().getWidth()+pd.getBoundingBox().getHeight());
            for(i=0; i<pd.getPocetBodu(); i++) {
                if ((i != preLastPoint) && (passThroughPointLeft[i]>0) && (pd.isLineBetween(lastPoint, i) || pd.getDistance(lastPoint,i)<minDist)) {
                    minPoint = i;
                    minDist = pd.getDistance(lastPoint,i);
                    if (pd.isLineBetween(lastPoint, i)) {
                        passThroughPointLeft[lastPoint]--;
                        passThroughPointLeft[minPoint]--;
                        break;
                    }
                }
            }

            numProcessed++;
            int l1, l2, x1, y1, x2, y2;
            for(j=0; j<pd.getPocetCar(); j++) {
                l1 = pd.getLines_1()[j];
                l2 = pd.getLines_2()[j];
                x1 = pd.getPoint_x()[l1];
                y1 = pd.getPoint_y()[l1];
                x2 = pd.getPoint_x()[l2];
                y2 = pd.getPoint_y()[l2];
                if (l1 == lastPoint && l2 == minPoint) {
                    p.addLine(x1, y1, x2, y2, pd.getPens()[j]);
                    break;
                }
                if (l2 == lastPoint && l1 == minPoint) {
                    p.addLine(x2, y2, x1, y1, pd.getPens()[j]);
                    break;
                }            
            }
            System.out.println("processed lines: " + numProcessed);
            preLastPoint = lastPoint;
            lastPoint = minPoint;
        }
        System.out.println("total lines = "+pd.getPocetCar() + "; zpracovano = "+numProcessed);
        p.calculatePathLengths();
        return p;
    }
   
}
