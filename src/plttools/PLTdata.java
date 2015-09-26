/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package plttools;

import com.sun.xml.internal.ws.message.saaj.SAAJHeader;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 *
 * @author vlada
 */
public class PLTdata {

    // input data
    protected int point_x[];
    protected int point_y[];
    protected int lines_1[];
    protected int lines_2[];
    /**
     * lineType -> 0 = straight line; 1 = circle; 2 = arc; 10.. = subplot, where [x-10] is index of subplot in 'subPlots' array
     */
    protected int lineType[];
    protected int selectedLines[] = new int[0];
    protected byte pen;
    protected byte status[];
    protected int linesCount;
    private int populatedLines = 0;

    //calculated data
    protected float distances[][];
    protected int lines_at_point[];
    private int travelsCount = 0;
    protected int pointsCount = 0;
    private double linesLength = 0.0;
    private double travelsLength = 0.0;
    protected Rectangle boundingBox;
    private PLTdata subPlots[] = new PLTdata[0];
    private int subPlotsCount = 0;

    protected PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);
    
    public void calculateDistances() {
        float dist;
        distances = new float[pointsCount][pointsCount];
        int i, j, k;
        propertySupport.firePropertyChange("progressMessage", null, "distance calculation");
        for (i=0; i<pointsCount; i++) {
            distances[i][i] = (float) (2*(boundingBox.getMaxX()+boundingBox.getMaxY()));
            for (j=0; j<i; j++) {
                dist = calculateDistance(i,j);
                distances[i][j] = dist;
                distances[j][i] = dist;
            }
            propertySupport.firePropertyChange("progressValue", 0, (int) ((100.0*i)/pointsCount));            
        }
        for (i=0; i<subPlotsCount; i++) {
            subPlots[i].calculateDistances();
        }
        System.gc();
//        System.out.println("Distances:");
//        DecimalFormat format = new DecimalFormat("#####");
//        for (i=0; i<pocetBodu; i++) {
//            for (j=0; j<i; j++) {
//                System.out.print(format.format(distances[i][j]) + "\t");
//            }
//            System.out.println();
//        }
        propertySupport.firePropertyChange("progressFinished", false, true);
    }

    public float calculateDistance(int point1, int point2) {
        float dx, dy;
        dx = point_x[point1] - point_x[point2];
        dy = point_y[point1] - point_y[point2];
        return (float) Math.sqrt(dx*dx + dy*dy);    
    }
    
    private boolean findLoop(int startIndex) {
        int foundPoint = lines_1[startIndex];
        int x2a;
        int y2a;
        int ka = startIndex;
        int numAt1;
        int numAt2;
        boolean searchNext = true;
        if (lines_at_point[lines_1[startIndex]]<2) {
            return false;
        }
        while (searchNext) {
            x2a = point_x[foundPoint];
            y2a = point_y[foundPoint];
            searchNext = false;
            for (int i=0; i<linesCount; i++) {
                if (i!=ka) {
                    if (point_x[lines_1[i]]==x2a && point_y[lines_1[i]]==y2a) {
                        numAt2 = lines_at_point[lines_2[i]];
                        if (numAt2 != 2) {
                            return false;
                        } else {
                            foundPoint = lines_2[i];
                            ka = i;
                            searchNext = true;
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
                            searchNext = true;
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

    /**
     * 1 = end in point 1
     * 2 = end in poing 2
     * 3 = part of closed loop
     * 4 = part of polyline
     * 5 = single line (end in both points 1 and 2)
     * 6 = error
     * @param index 
     * @param loopStart
     * @return status
     */
    private byte checkStatus(int index) {
        
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
    
    public void calculateStats() {
//        propertySupport.firePropertyChange("progressMessage", null, "loops detection and lengths calculation");
        double thisTravel;
        travelsCount = 0;
        travelsLength = 0;
        linesLength = 0;
        for (int i=0; i < linesCount; i++) {
//            propertySupport.firePropertyChange("progressValue", 0, (int) ((100.0*k)/pocetCar));            
            if (lineType[i] >= 10) { // subplot
                subPlots[lineType[i]-10].calculateStats();
                linesLength += subPlots[lineType[i]-10].linesLength;
                travelsCount += subPlots[lineType[i]-10].travelsCount;
                travelsLength += subPlots[lineType[i]-10].travelsLength;
            } else {
                linesLength += Math.sqrt(Math.pow(point_x[lines_2[i]] - point_x[lines_1[i]], 2) + Math.pow(point_y[lines_2[i]] - point_y[lines_1[i]], 2));
                status[i] = checkStatus(i);
    //            System.out.println("status of " + k + " = "+status[k]+"; lines at point 1 = "+lines_at_point[lines_1[k]]+"; lines at point 2 = "+lines_at_point[lines_2[k]]+"; pen = " +pens[k]);
                if (i>1) {
                    thisTravel = Math.sqrt(Math.pow(point_x[lines_1[i]] - point_x[lines_2[i-1]], 2) + Math.pow(point_y[lines_1[i]] - point_y[lines_2[i-1]], 2));
                    if (thisTravel>0) {
                        travelsLength += thisTravel;
                        travelsCount++;
                    }
                }
            }
        }
//        propertySupport.firePropertyChange("progressFinished", false, true);
    }

    public void setLineCount(int lineCount) {
        linesCount = lineCount;
        populatedLines = 0;
        status = new byte[linesCount];
        lines_1 = new int[linesCount];
        lines_2 = new int[linesCount];
        lineType = new int[linesCount];
        point_x = new int[2*linesCount+1];
        point_y = new int[2*linesCount+1];
        lines_at_point = new int[2*linesCount+1];        
        for (int j=0; j<2*linesCount+1; j++) {
            lines_at_point[j] = 0;
        }
    }
    
    public void addLine(int startX, int startY, int endX, int endY) {
        System.out.println("add line #"+populatedLines+" [" + startX + ";" + startY + "] - [" + endX + ";" + endY + "]; pen = "+ pen);
//        System.out.println("number of points = " + pocetBodu);
        int startId = getPointId(startX, startY, true);
        int endId = getPointId(endX, endY, true);
//        System.out.println("startId = " + startId + "; endId = " + endId);
        if (populatedLines >= linesCount) {
            lines_1 = inflateIntArray(lines_1,100);
            lines_2 = inflateIntArray(lines_2,100);
            lineType = inflateIntArray(lineType,100);
            status = inflateByteArray(status,100);
            linesCount += 100;
        }
        lines_1[populatedLines] = startId;
        lines_2[populatedLines] = endId;
        lineType[populatedLines] = 0;
        populatedLines++;
    }

    public void addLine(int startX, int startY, int endX, int endY, int type) {
        addLine(startX, startY, endX, endY);
        lineType[populatedLines-1] = type;
    }   
    
    private int getPointId(int x, int y, boolean createNew) {
        for (int i=0; i < pointsCount; i++) {
            if (point_x[i] == x && point_y[i] == y) {
                if (createNew) {
                    lines_at_point[i]++;
                }
                return i;
            }
        }
        if (createNew) {
//            System.out.println("create new [" + x + ";" + y + "]");
            if (boundingBox == null) {
                boundingBox = new Rectangle(new Point(x,y));
            } else if (! boundingBox.contains(x, y)) {
                boundingBox.add(x, y);
            }
            point_x[pointsCount] = x;
            point_y[pointsCount] = y;
            lines_at_point[pointsCount] = 1;
            pointsCount++;
            return pointsCount - 1;
        } else {
            return -1;
        }
    }

    public boolean isLineBetween(int point1, int point2) {
        for (int i = 0; i < populatedLines; i++) {
            if ((lines_1[i] == point1 && lines_2[i] == point2) 
                 || (lines_2[i] == point1 && lines_1[i] == point2)) {
                return true;
            }
        }
        return false;
    }
    
    public float getDistance(int point1, int point2) {
        return distances[point1][point2];
    }

    public byte getLinesAtPoint(int point) {
        return (byte) lines_at_point[point];
    }
    
    public float getLengthOfLine(int lineNum) {
        return calculateDistance(lines_1[lineNum], lines_2[lineNum]);
    }
    
    // claculates angle between given lines
    public double angleBetweenLines(int line1, int line2) {
        int u1 = point_x[lines_2[line1]] - point_x[lines_1[line1]];
        int u2 = point_y[lines_2[line1]] - point_y[lines_1[line1]];
        int v1 = point_x[lines_2[line2]] - point_x[lines_1[line2]];
        int v2 = point_y[lines_2[line2]] - point_y[lines_1[line2]];
        
        return Math.acos(Math.abs(u1*v1 + u2*v2) / (Math.sqrt(u1*u1+u2*u2)*Math.sqrt(v1*v1+v2*v2))) * 180/Math.PI;
        
    }
    
    // claculates distance of point pointNum to (indefinite) line lineNum
    public float getDistanceOfPointFromLine(int pointNum, int lineNum) {
        
        int m = point_x[pointNum];
        int n = point_y[pointNum];
        
        
        // and finally apply the equation for calculation of distance of point
        return _getDistanceOfPoint(m, n, lineNum);
    }
    
    private float _getDistanceOfPoint(int m, int n, int lineNum) {
        // first we need to calculate line equation in general form
        int x1 = point_x[lines_1[lineNum]];
        int y1 = point_y[lines_1[lineNum]];
        int x2 = point_x[lines_2[lineNum]];
        int y2 = point_y[lines_2[lineNum]];
        
        int a = y2 - y1;
        int b = -(x2 - x1);
        float c = -x1*a - y1*b;

        // and finally apply the equation for calculation of distance of point
        return (float) (Math.abs(a*m + b*n + c) / Math.sqrt(a*a + b*b));
    }
    
    // calculates coordinates of point X on line lineNum, which is closest to another point pointNum
    // the distance between point X and pointNum is getDistanceOfPointFromLine(pointNum, lineNum)
    public Point getClosestPointOnLine(int pointNum, int lineNum) {
        // first we need to calculate line equation in general form
        int x1 = point_x[lines_1[lineNum]];
        int y1 = point_y[lines_1[lineNum]];
        int x2 = point_x[lines_2[lineNum]];
        int y2 = point_y[lines_2[lineNum]];
        
        int m = point_x[pointNum];
        int n = point_y[pointNum];
        
        int a = y2 - y1;
        int b = -(x2 - x1);
        float c = -x1*a - y1*b;        
        
        // and finally calculate coordinates of point X
        int x;
        int y;
        if (b != 0) {
            x = Math.round((b*b*m - a*b*n - c*a) / (b*b + a*a));
            y = Math.round((-c - a*x) / b);
        } else {            
            y = Math.round(-(a*b*m - a*a*n + c*b) / (b*b + a*a));
            x = Math.round((-c - b*y) / a);
        }
        return new Point(x, y);
    }
    
    public boolean isPointInLine(Point p, int lineNum) {
        int x1 = point_x[lines_1[lineNum]];
        int y1 = point_y[lines_1[lineNum]];
        int x2 = point_x[lines_2[lineNum]];
        int y2 = point_y[lines_2[lineNum]];
        Rectangle lineRange = new Rectangle(Math.min(x1,x2)-1, Math.min(y1,y2)-1, Math.abs(x2-x1)+2,Math.abs(y2-y1)+2);
        if (_getDistanceOfPoint(p.x, p.y, lineNum) > 0.1) {
            System.out.println("point " + p + " not on line #" + lineNum + "(distance = "+_getDistanceOfPoint(p.x, p.y, lineNum)+")");
            return false;
        } else if (lineRange.contains(p.x, p.y)) {
            return true;
        } else {
            System.out.println("point " + p + " not between endpoints of line #" + lineNum + " - ["+x1+";"+y1+"] - ["+x2+";"+y2+"]");
            return false;
        }
    }
    
    public double getLinesLength() {
        return linesLength;
    }

    public double getTravelsLength() {
        return travelsLength;
    }


    public int getTravelsCount() {
        return travelsCount;
    }

    public int getPointsCount() {
        return pointsCount;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
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

    public int getLinesCount() {
        return linesCount;
    }

    public int getPopulatedLines() {
        return populatedLines;
    }

    public byte getPen() {
        return pen;
    }

    public void setPen(byte pen) {
        this.pen = pen;
    }

    public byte[] getStatus() {
        return status;
    }
    
    /**
     * Return status of point
     * 1 - endpoint
     * 2 - part of contignuous line
     * 3 - part of closed loop
     * 4 - error
     * @param point index of searched point
     * @return status
     */
    public byte getStatusAtPoint(int point) {
        for (int i=0; i<linesCount; i++) {
            if (point == lines_1[i] || point == lines_2[i]) {
                if ((status[i] == 5)
                        || (point == lines_1[i] && status[i] == 1)
                        || (point == lines_2[i] && status[i] == 2)) {
                    return (byte) 1;
                } else if (status[i] == 4) {
                    return (byte) 2;
                } else if (status[i] == 3) {
                    return (byte) 3;
                } else {
                    return (byte) 4;
                }
            }
        }
        return (byte) 3;
    }

    /**
     * returns information, if any line is ending at given point
     * @param point point index number
     * @return point is endopoint (true or false)
     */
    public boolean isEndPoint(int point) {
        return getStatusAtPoint(point) == 1;
    }
    
    public Rectangle getBoundingBox() {
        return boundingBox;
    }

    @Override
    protected Object clone() {
        PLTdata p = new PLTdata();
        p.point_x = new int[2*linesCount+1];
        p.point_y = new int[2*linesCount+1];
        p.lines_at_point = new int[2*linesCount+1];
        for (int i=0; i<2*linesCount+1; i++) {
            p.point_x[i] = point_x[i];
            p.point_y[i] = point_y[i];
            p.lines_at_point[i] = lines_at_point[i];
        }
        p.linesCount = linesCount;
        p.pointsCount = pointsCount;
        p.boundingBox = (Rectangle) boundingBox.clone();
        p.pen = this.pen;
        p.status = new byte[linesCount];
        p.lines_1 = new int[linesCount];
        p.lines_2 = new int[linesCount];
        p.lineType = new int[linesCount];
        for (int i=0; i<linesCount; i++) {
            p.lines_1[i] = lines_1[i];
            p.lines_2[i] = lines_2[i];
            p.lineType[i] = lineType[i];
        }
        p.subPlotsCount = subPlotsCount;
        p.subPlots = new PLTdata[subPlotsCount];
        for (int i=0; i<subPlotsCount; i++) {
            p.subPlots[i] = (PLTdata) subPlots[i].clone();  
        }
        return p;
    }
    
    public void deleteLine(int lineNum) {
        for (int i=0; i<populatedLines-1; i++) {
            if (lineNum<=i) {
                lines_1[i] = lines_1[i+1];
                lines_2[i] = lines_2[i+1];
                lineType[i] = lineType[i+1];
                status[i] = status[i+1];
            }
        }
        for (int i=0; i<selectedLines.length; i++) {
            if (lineNum<selectedLines[i]) {
                selectedLines[i]--;
            }
        }
        populatedLines--;
    }
    
    public void setSelection(int lines[]) {
        selectedLines = lines;
    }
    
    public void addSelection(int lines[]) {
        int newSel[] = new int[selectedLines.length + lines.length];
        System.arraycopy(selectedLines, 0, newSel, 0, selectedLines.length);
        System.arraycopy(lines, 0, newSel, selectedLines.length, lines.length);
        selectedLines = newSel;
    }
    
    public void deleteSelection(int lines[]) {
        int newSel[] = new int[selectedLines.length - lines.length];
        boolean contains;
        int position = 0;
        for (int i: selectedLines) {
            contains = false;
            for (int j: lines) {
                if (i == j) {
                    contains = true;
                    break;
                }
            }
            
            if (! contains) {
                newSel[position++] = i;
            }
        }
        selectedLines = newSel;
    }
    
    public boolean isInSelection(int lineNum) {
        for (int i:selectedLines) {
            if (i == lineNum) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Moves lines referenced in 'slectedLines' to unique subplot
     */
    public void makeSubPlotFromSelection() {        
        if (selectedLines.length > 0) {
            subPlotsCount++;
            PLTdata newSubPlots[] = new PLTdata[subPlotsCount];
            System.arraycopy(subPlots, 0, newSubPlots, 0, subPlots.length);
            PLTdata newSubPlot = new PLTdata();

            newSubPlot.setLineCount(selectedLines.length);
            newSubPlot.setPen(getPen());
            for (int i=0; i<selectedLines.length; i++) {
                newSubPlot.addLine(point_x[lines_1[selectedLines[i]]],
                                   point_y[lines_1[selectedLines[i]]],
                                   point_x[lines_2[selectedLines[i]]],
                                   point_y[lines_2[selectedLines[i]]]);
            }
            
            addLine(point_x[lines_1[selectedLines[0]]],
                    point_y[lines_1[selectedLines[0]]],
                    point_x[lines_2[selectedLines[selectedLines.length-1]]],
                    point_y[lines_2[selectedLines[selectedLines.length-1]]],
                    10+subPlotsCount-1);
            
            for (int i=0; i<selectedLines.length; i++) {
                deleteLine(selectedLines[i]);
            }
            
            selectedLines = new int[0];

            newSubPlots[subPlotsCount-1] = newSubPlot;
            subPlots = newSubPlots;
        }
    }

    public PLTdata[] getSubPlots() {
        return subPlots;
    }    
    
    public boolean hasSelection() {
        return selectedLines.length > 0;
    }
    
    private int[] inflateIntArray(int[] oldArray, int size) {
        int newArray[] = new int[oldArray.length+size];
        System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
        return newArray;        
    }

    private byte[] inflateByteArray(byte[] oldArray, int size) {
        byte newArray[] = new byte[oldArray.length+size];
        System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
        return newArray;        
    }

    public int[] getLineType() {
        return lineType;
    }
        
} 
