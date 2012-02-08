/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package plttools;

import java.awt.Color;
import java.awt.Graphics;
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
            
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (plt != null) {
            int x1[] = plt.getX1();
            int y1[] = plt.getY1();
            int x2[] = plt.getX2();
            int y2[] = plt.getY2();
            int pens[] = plt.getPens();
            int status[] = plt.getStatus();
            int pocet = plt.getPocetCar();        
            scale = Math.min(1.0 * (getWidth()-2*margin)/ plt.getMax_x(), 1.0 * (getHeight()-2*margin) / plt.getMax_y());

            g.setColor(Color.WHITE);
            g.fillRect(transformX(0), transformY(plt.getMax_y()), transformX(plt.getMax_x()) - transformX(0), transformY(0) - transformY(plt.getMax_y()));
            //System.out.println("drawing; scale = " + scale + "x1 size = " + x1.length);

            int lastX = 0;
            int lastY = 0;
            for (int i = 0; i < pocet; i++) {
                if (kreslitPrejezdy && (x1[i] != lastX || y1[i] != lastY)) {
                    g.setColor(getColorForPen(-1));
                    g.drawLine(transformX(lastX), transformY(lastY),
                               transformX(x1[i]),transformY(y1[i]));                    
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
                g.drawLine(transformX(x1[i]), transformY(y1[i]),
                           transformX(x2[i]), transformY(y2[i]));   
                lastX = x2[i];
                lastY = y2[i];
//                if (i <= 5) {
//                    System.out.println("i="+i+"; x1="+transformX(x1[i])+";y1="+transformY(y1[i])
//                           +";x2="+transformX(x2[i])+";y2="+transformY(y2[i]));
//                }
            }
        }        
    }

    private int transformX(double x) {
        return (int) (x * scale) + margin;
    }

    private int transformY(double y) {
        return getHeight() - (int) (y * scale) - margin;
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
        repaint();
    }

    public void setKreslitPrejezdy(boolean kreslitPrejezdy) {
        this.kreslitPrejezdy = kreslitPrejezdy;
    }
    
}
