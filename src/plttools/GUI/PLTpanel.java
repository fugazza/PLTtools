/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package plttools.GUI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.*;
import javax.swing.JPanel;
import plttools.PLTdata;

/**
 *
 * @author Vláďa
 */
public class PLTpanel extends JPanel {

    private PLTdata plt[];
    private double scale = 1.0;
    private final int margin = 3;
    private boolean kreslitPrejezdy = true;
    private boolean kreslitStatus = false;
    private boolean drawDebug = false;
    private double centerX;  // int plot units
    private double centerY;  // int plot units
    private int maxX;  // int plot units
    private int maxY;  // int plot units
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
            // draw the background for area for output of plot
            g.setColor(Color.WHITE);
            g.fillRect(transformX(0), transformY(maxY), transformX(maxX) - transformX(0), transformY(0) - transformY(maxY));
//            System.out.println("drawing; scale = " + scale + "maxX = " + plt.getBoundingBox().getMaxX());
            for (PLTdata p: plt) {
                int lines_1[] = p.getLines_1();
                int lines_2[] = p.getLines_2();
                int point_x[] = p.getPoint_x();
                int point_y[] = p.getPoint_y();
                byte status[] = p.getStatus();
                int pocet = p.getPopulatedLines();        

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
                        g.setColor(getColorForPen(p.getPen()));
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
                    for (int i=0; i< p.getPocetBodu(); i++) {
                        pX = transformX(p.getPoint_x()[i]);
                        pY = transformY(p.getPoint_y()[i]);
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
    }

    private int transformX(double x) {
        return (int) (getWidth()/2 + ((x-centerX) * scale));
    }

    private int transformY(double y) {
        return (int) (getHeight()/2 - ((y-centerY) * scale));
    }

    public static Color getColorForPen(int pen) {
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

    public void setPlt(PLTdata plt[]) {
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
        double origCenterX = transformX(centerX);
        double origCenterY = transformY(centerY);
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
        maxX = 0;
        maxY = 0;
        // get maximum X and Y from all plots
        if (plt != null) {
            for(PLTdata p: plt) {
//                System.out.println("stats for pen "+ p.getPen()+"; lins count = "+ p.getPocetCar()+"; bounding box = " + p.getBoundingBox());
                if (p.getBoundingBox().getMaxX() > maxX) {
                    maxX = (int) p.getBoundingBox().getMaxX();
                }
                if (p.getBoundingBox().getMaxY() > maxY) {
                    maxY = (int) p.getBoundingBox().getMaxY();
                }
            }
        } else {
            maxX = 10;
            maxY = 10;
        }
        scale = Math.min(1.0 * (getWidth()-2*margin)/ maxX, 1.0 * (getHeight()-2*margin) / maxY);
        centerX = (maxX / 2);
        centerY = (maxY / 2);                  
    }
    
    private void panelMouseDragged(MouseEvent e) {
        centerX -= (e.getX() - dragPoint.x) / scale;
        centerY += (e.getY() - dragPoint.y) / scale;
        dragPoint.setLocation(e.getPoint());
        repaint();
    }
}
