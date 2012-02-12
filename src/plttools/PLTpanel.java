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

    private PLTfile plt = null;
    private double scale = 1.0;
    private final int margin = 3;
    private boolean kreslitPrejezdy = true;
    private boolean kreslitStatus = false;
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
            int pens[] = plt.getPens();
            int status[] = plt.getStatus();
            int pocet = plt.getPocetCar();        

            g.setColor(Color.WHITE);
            g.fillRect(transformX(0), transformY(plt.getMax_y()), transformX(plt.getMax_x()) - transformX(0), transformY(0) - transformY(plt.getMax_y()));
            //System.out.println("drawing; scale = " + scale + "x1 size = " + x1.length);

            int lastX = 0;
            int lastY = 0;
            int lastPoint = -1;
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
                lastX = point_x[lines_2[i]];
                lastY = point_y[lines_2[i]];
                lastPoint = lines_2[i];
//                if (i <= 5) {
//                    System.out.println("i="+i+"; x1="+transformX(x1[i])+";y1="+transformY(y1[i])
//                           +";x2="+transformX(x2[i])+";y2="+transformY(y2[i]));
//                }
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

    public void setPlt(PLTfile plt) {
        this.plt = plt;
        setAutoScaleAndCenter();           
        repaint();
    }

    public void setKreslitPrejezdy(boolean kreslitPrejezdy) {
        this.kreslitPrejezdy = kreslitPrejezdy;
    }

    public void setKreslitStatus(boolean kreslitStatus) {
        this.kreslitStatus = kreslitStatus;
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
        scale = Math.min(1.0 * (getWidth()-2*margin)/ plt.getMax_x(), 1.0 * (getHeight()-2*margin) / plt.getMax_y());
        centerX = plt.getMax_x()/2;
        centerY = plt.getMax_y()/2;                    
    }
    
    private void panelMouseDragged(MouseEvent e) {
        centerX -= (e.getX() - dragPoint.x) / scale;
        centerY += (e.getY() - dragPoint.y) / scale;
        dragPoint.setLocation(e.getPoint());
        repaint();
    }
}
