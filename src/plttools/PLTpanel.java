/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package plttools;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.*;
import javax.swing.JPanel;

/**
 *
 * @author Vláďa
 */
public class PLTpanel extends JPanel {

    private PLTdata plt = null;
    private double scale = 1.0;
    private final int margin = 3;
    private boolean kreslitPrejezdy = true;
    private boolean kreslitStatus = false;
    private boolean drawDebug = false;
    private int centerX;
    private int centerY;
    private Point dragPoint = new Point(0,0);

    public PLTpanel() {
        super();
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                panelMouseWheelMoved(e);
            }
            
        });
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                panelMouseClicked(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                dragPoint.setLocation(e.getPoint());
            }

        });
        addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                panelMouseDragged(e);
            }

        });
    }
            
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (plt != null) {
            int lines_1[] = plt.getLines_1();
            int lines_2[] = plt.getLines_2();
            int point_x[] = plt.getPoint_x();
            int point_y[] = plt.getPoint_y();
            byte pens[] = plt.getPens();
            byte status[] = plt.getStatus();
            int pocet = plt.getPopulatedLines();        

            // draw the background for area for output of plot
            g.setColor(Color.WHITE);
            g.fillRect(transformX(0), transformY(plt.getBoundingBox().getMaxY()), transformX(plt.getBoundingBox().getMaxX()) - transformX(0), transformY(0) - transformY(plt.getBoundingBox().getMaxY()));
//            System.out.println("drawing; scale = " + scale + "maxX = " + plt.getBoundingBox().getMaxX());

            // dimension of info box for displaying numbers of lines and points
            int infoW = 25, infoH = 15;
            
            // draw all lines
            int lastX = 0;
            int lastY = 0;
            int lastPoint = -1;
            int midpointX;
            int midpointY;
            for (int i = 0; i < pocet; i++) {
                if (kreslitPrejezdy && (lines_1[i] != lastPoint)) {
                    g.setColor(getColorForPen(-1));
                    g.drawLine(transformX(lastX), transformY(lastY),
                               transformX(point_x[lines_1[i]]),transformY(point_y[lines_1[i]]));                    
//                    if (i <= 5) {
//                        System.out.println("ir="+i+"; x1="+transformX(x2[i-1])+";y1="+transformY(y2[i-1])
//                               +";x2="+transformX(x1[i])+";y2="+transformY(y1[i]));
//                    }
                }
                if (kreslitStatus) {
                    g.setColor(getColorForStatus(status[i]));
                } else {
                    g.setColor(getColorForPen(pens[i]));
                }
                g.drawLine(transformX(point_x[lines_1[i]]), transformY(point_y[lines_1[i]]),
                           transformX(point_x[lines_2[i]]), transformY(point_y[lines_2[i]]));   
                
                if (drawDebug) {
                    midpointX = transformX((point_x[lines_1[i]] + point_x[lines_2[i]])/2);
                    midpointY = transformY((point_y[lines_1[i]] + point_y[lines_2[i]])/2);
                    g.setColor(Color.WHITE);
                    g.fillRect(midpointX - infoW/2, midpointY - infoH/2, infoW, infoH);
                    g.setColor(Color.BLACK);
                    g.drawRect(midpointX - infoW/2, midpointY - infoH/2, infoW, infoH);
                    g.drawString(Integer.toString(i), midpointX - infoW/2+2, midpointY + infoH/2-2);
                }
                lastX = point_x[lines_2[i]];
                lastY = point_y[lines_2[i]];
                lastPoint = lines_2[i];
//                if (i <= 5) {
//                    System.out.println("i="+i+"; x1="+transformX(x1[i])+";y1="+transformY(y1[i])
//                           +";x2="+transformX(x2[i])+";y2="+transformY(y2[i]));
//                }
            }
            
            // draw marks at points if debug info selected
            if(drawDebug) {
                int pX, pY;
                for (int i=0; i< plt.getPocetBodu(); i++) {
                    pX = transformX(plt.getPoint_x()[i]);
                    pY = transformY(plt.getPoint_y()[i]);
                    // fill the area for label
                    g.setColor(Color.WHITE);
                    g.fillRect(pX - infoW/2, pY - infoH - 5, infoW, infoH);
                    // draw border of label
                    g.setColor(Color.BLUE);
                    g.drawRect(pX - infoW/2, pY - infoH - 5, infoW, infoH);
                    // draw connecting line
                    g.drawLine(pX, pY, pX, pY-5);
                    // and outpt the text
                    g.drawString(Integer.toString(i), pX - infoW/2+2, pY - 5 - 2);
                }
            }
        }        
    }

    private int transformX(double x) {
        return getWidth()/2 + (int) ((x-centerX) * scale);
    }

    private int transformY(double y) {
        return getHeight()/2 - (int) ((y-centerY) * scale);
    }

    private Color getColorForPen(int pen) {
        switch (pen) {
            case -1:
                return Color.RED;
            case 1:
                return Color.BLACK;
            case 2:
                return Color.YELLOW;
            case 3:
                return Color.RED;
            case 4:
                return Color.GREEN;
            case 5:
                return Color.BLUE;
            case 6:
                return Color.ORANGE;
            case 0:
            default:
                return Color.BLACK;
        }
    }
    
    private Color getColorForStatus(int status) {
        switch (status) {
            case 1:
                return Color.BLUE;
            case 2:
                return Color.CYAN;
            case 3:
                return Color.ORANGE;
            case 4:
                return Color.BLACK;
            case 5:
                return Color.GREEN;
            case 6:
                return Color.RED;
            case 0:
            default:
                return Color.GRAY;
        }
    }

    public void setPlt(PLTdata plt) {
        this.plt = plt;
        setAutoScaleAndCenter();           
        repaint();
    }

    public void setKreslitPrejezdy(boolean kreslitPrejezdy) {
        this.kreslitPrejezdy = kreslitPrejezdy;
        repaint();
    }

    public void setKreslitStatus(boolean kreslitStatus) {
        this.kreslitStatus = kreslitStatus;
        repaint();
    }

    public void setDrawDebug(boolean drawDebug) {
        this.drawDebug = drawDebug;
        repaint();
    }
    
    private void panelMouseWheelMoved(MouseWheelEvent e) {
        double origScale = scale;
        int origCenterX = transformX(centerX);
        int origCenterY = transformY(centerY);
        scale *= (1 - e.getWheelRotation() * 0.1);
        centerX -= ((e.getX() - origCenterX) *(origScale - scale) / (scale * origScale));
        centerY += ((e.getY() - origCenterY) *(origScale - scale) / (scale * origScale));
        repaint();
    }
    
    private void panelMouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            setAutoScaleAndCenter();   
            repaint();
        }
    }
    
    private void setAutoScaleAndCenter() {
        scale = Math.min(1.0 * (getWidth()-2*margin)/ plt.getBoundingBox().getMaxX(), 1.0 * (getHeight()-2*margin) / plt.getBoundingBox().getMaxY());
        centerX = (int) (plt.getBoundingBox().getMaxX() / 2);
        centerY = (int) (plt.getBoundingBox().getMaxY() / 2);                  
    }
    
    private void panelMouseDragged(MouseEvent e) {
        centerX -= (e.getX() - dragPoint.x) / scale;
        centerY += (e.getY() - dragPoint.y) / scale;
        dragPoint.setLocation(e.getPoint());
        repaint();
    }
}
