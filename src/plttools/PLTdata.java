/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package plttools;

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
    protected byte pen;
    protected byte status[];
    protected int pocetCar;
    private int populatedLines = 0;

    //calculated data
    protected float distances[][];
    protected int lines_at_point[];
    private int pocetPrejezdu = 0;
    protected int pocetBodu = 0;
    private double delkaCar = 0.0;
    private double delkaPrejezdu = 0.0;
    protected Rectangle boundingBox;

    protected PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);
    
    public void calculateDistances() {
        float dist;
        distances = new float[pocetBodu][pocetBodu];
        int i, j, k;
        propertySupport.firePropertyChange("progressMessage", null, "distance calculation");
        for (i=0; i<pocetBodu; i++) {
            distances[i][i] = (float) (2*(boundingBox.getMaxX()+boundingBox.getMaxY()));
            for (j=0; j<i; j++) {
                dist = calculateDistance(i,j);
                distances[i][j] = dist;
                distances[j][i] = dist;
            }
            propertySupport.firePropertyChange("progressValue", 0, (int) ((100.0*i)/pocetBodu));            
        }
        propertySupport.firePropertyChange("progressMessage", null, "loops detection");
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

    public void calculatePathLengths() {
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
    
    public void calculateStats() {
        double thisTravel;
        pocetPrejezdu = 0;
        delkaPrejezdu = 0;
        delkaCar = 0;
        for (int k=0; k < pocetCar; k++) {
            delkaCar += Math.sqrt(Math.pow(point_x[lines_2[k]] - point_x[lines_1[k]], 2) + Math.pow(point_y[lines_2[k]] - point_y[lines_1[k]], 2));
            status[k] = checkStatus(k);
//            System.out.println("status of " + k + " = "+status[k]+"; lines at point 1 = "+lines_at_point[lines_1[k]]+"; lines at point 2 = "+lines_at_point[lines_2[k]]+"; pen = " +pens[k]);
            if (k>1) {
                thisTravel = Math.sqrt(Math.pow(point_x[lines_1[k]] - point_x[lines_2[k-1]], 2) + Math.pow(point_y[lines_1[k]] - point_y[lines_2[k-1]], 2));
                if (thisTravel>0) {
                    delkaPrejezdu += thisTravel;
                    pocetPrejezdu++;
                }
            }
        }
    }

    public void setLineCount(int lineCount) {
        pocetCar = lineCount;
        populatedLines = 0;
        status = new byte[pocetCar];
        lines_1 = new int[pocetCar];
        lines_2 = new int[pocetCar];
        point_x = new int[2*pocetCar+1];
        point_y = new int[2*pocetCar+1];
        lines_at_point = new int[2*pocetCar+1];        
        for (int j=0; j<2*pocetCar+1; j++) {
            lines_at_point[j] = 0;
        }
    }
    
    public void addLine(int startX, int startY, int endX, int endY) {
        System.out.println("add line #"+populatedLines+" [" + startX + ";" + startY + "] - [" + endX + ";" + endY + "]; pen = "+ pen);
//        System.out.println("number of points = " + pocetBodu);
        int startId = getPointId(startX, startY, true);
        int endId = getPointId(endX, endY, true);
//        System.out.println("startId = " + startId + "; endId = " + endId);
        lines_1[populatedLines] = startId;
        lines_2[populatedLines] = endId;
        populatedLines++;
    }

    private int getPointId(int x, int y, boolean createNew) {
        for (int i=0; i < pocetBodu; i++) {
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
            point_x[pocetBodu] = x;
            point_y[pocetBodu] = y;
            lines_at_point[pocetBodu] = 1;
            pocetBodu++;
            return pocetBodu - 1;
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
    
    public double getDelkaCar() {
        return delkaCar;
    }

    public double getDelkaPrejezdu() {
        return delkaPrejezdu;
    }


    public int getPocetPrejezdu() {
        return pocetPrejezdu;
    }

    public int getPocetBodu() {
        return pocetBodu;
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

    public int getPocetCar() {
        return pocetCar;
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
        for (int i=0; i<pocetCar; i++) {
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
        p.boundingBox = (Rectangle) boundingBox.clone();
        p.pen = this.pen;
        p.status = new byte[pocetCar];
        p.lines_1 = new int[pocetCar];
        p.lines_2 = new int[pocetCar];
        
        return p;
    }
}
