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
public class ModifiedGreedyOptimizer extends AbstractOptimizer {

    @Override
    public PLTdata optimize() {
        propertySupport.firePropertyChange("progressMessage", null, "modified greedy optimization");
        PLTdata p = new PLTdata();
        boolean processed[] = new boolean[pd.getPopulatedLines()];
        int numProcessed = 0;
        int lastX2 = 0, lastY2 = 0;
        int range = 0;
        int rangeNulling = 0;
        int vzdalenost1x, vzdalenost1y, vzdalenost2x, vzdalenost2y, vzdalenost1, vzdalenost2;
        int l1, l2, x1, y1, x2, y2;
        p.setLineCount(pd.getPopulatedLines());
        p.setPen(pd.getPen());
        while (numProcessed < pd.getPopulatedLines()) {
            System.out.println("processed="+numProcessed+"; range = "+range);
            propertySupport.firePropertyChange("progressValue", 0, (int) ((100.0*numProcessed)/pd.getPopulatedLines()));                            
            boolean findLineStart = true;
            if (rangeNulling > 10) {
                findLineStart = false;
//                rangeNulling = 0;
            }
            boolean haveLine;
            do {
                haveLine = false;
                for (int j=0; j<pd.getPopulatedLines(); j++) {
                    if (processed[j]) {
//                    if (processed[j]) {
                        continue;
                    }
                    l1 = pd.getLines_1()[j];
                    l2 = pd.getLines_2()[j];
                    x1 = pd.getPoint_x()[l1];
                    y1 = pd.getPoint_y()[l1];
                    x2 = pd.getPoint_x()[l2];
                    y2 = pd.getPoint_y()[l2];
                    vzdalenost1x = Math.abs(x1 - lastX2);
                    vzdalenost1y = Math.abs(y1 - lastY2);
                    vzdalenost2x = Math.abs(x2 - lastX2);
                    vzdalenost2y = Math.abs(y2 - lastY2);
                    vzdalenost1 = (int) Math.sqrt(vzdalenost1x*vzdalenost1x + vzdalenost1y*vzdalenost1y);
                    vzdalenost2 = (int) Math.sqrt(vzdalenost2x*vzdalenost2x + vzdalenost2y*vzdalenost2y);
//                    if ((Math.sqrt(1.0*Math.pow(x1[j] - lastX2,2) + Math.pow(y1[j] - lastY2,2)) <= range)
//                            && (!findLineStart || (status[j]!=2 && status[j]!=4))) {
//                    if ((Math.abs(x1[j] - lastX2) <= range) && (Math.abs(y1[j] - lastY2) <= range)) {
                    if (vzdalenost1==0 || vzdalenost2==0 || ((vzdalenost1 <= range) && (vzdalenost2 <= range)
                            && (!findLineStart || pd.getStatus()[j]!=4))) {
                        
                        if (vzdalenost1==0 || (vzdalenost2!=0 && ((pd.getStatus()[j]==1) || (vzdalenost1<=vzdalenost2 && pd.getStatus()[j]!=2)))) {
                            p.addLine(x1, y1, x2, y2);
                            lastX2 = x2;
                            lastY2 = y2;
                        } else {
                            p.addLine(x2, y2, x1, y1);
                            lastX2 = x1;
                            lastY2 = y1;
                        }
                        
                        processed[j] = true;
                        numProcessed++;
                        haveLine = true;
                        findLineStart = false;
                        range = 0;
                        break;
                    }
                }
                System.out.println("last processed = "+numProcessed);
            } while (haveLine);
            
            if (range == 0) {
                range = 2;
                //range = max_x + max_y;
            } else if (range > (Math.max(pd.getBoundingBox().getWidth()+pd.getBoundingBox().getHeight(),
                                         pd.getBoundingBox().getMaxX()+pd.getBoundingBox().getMaxY()))) {
                range = 0;
                lastX2 = 0;
                lastY2 = 0;
                rangeNulling++;
            } else {
                range = (int) Math.round(range*1.3);
                //range = max_x + max_y;
            }
        }
        
        p.calculateStats();
        propertySupport.firePropertyChange("progressFinished", false, true);
        return p;
    }

    @Override
    public boolean changesLineCount() {
        return false;
    }
    
}
