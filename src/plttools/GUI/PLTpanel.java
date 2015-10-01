/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package plttools.GUI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.TransferHandler;
import plttools.PLTdata;
import plttools.PLTfile;

/**
 *
 * @author Vláďa
 */
public class PLTpanel extends JPanel {

    private PLTfile plt;
    private double scale = 1.0;
    private final int margin = 3;
    private boolean kreslitPrejezdy = true;
    private boolean kreslitStatus = false;
    private boolean drawDebug = false;
    private boolean displayScale = true;
    private double centerX;  // int plot units
    private double centerY;  // int plot units
    private int maxX;  // int plot units
    private int maxY;  // int plot units
    private Point dragPoint = new Point(0,0);
    private int highlightedLine = -1;
    private int highlightedPen = -1;
    private boolean panMode = true;
    private boolean selectMode = false;
    private Rectangle selectionRectangle = null;
    private Point startSelectionPoint = null;

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
                if (panMode) {
                    dragPoint.setLocation(e.getPoint());
                }
                if (selectMode) {
                    startSelectionPoint = e.getPoint();
                }
                
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (selectionRectangle != null) {
                    System.out.println("mouse button released");
                    Rectangle.Double selectionInDataSpace = new Rectangle.Double(backTransformX(selectionRectangle.x), backTransformY(selectionRectangle.y), 0, 0);
                    selectionInDataSpace.add(backTransformX(selectionRectangle.x + selectionRectangle.width),
                                             backTransformY(selectionRectangle.y + selectionRectangle.height));
                    
                    System.out.println("selection rectangle = " + selectionInDataSpace);
                    for (PLTdata p: plt.getPltData()) {
                        int selCount = 0;
                        int lines_1[] = p.getLines_1();
                        int lines_2[] = p.getLines_2();
                        int point_x[] = p.getPoint_x();
                        int point_y[] = p.getPoint_y();
                        int pocet = p.getPopulatedLines();  

                        for (int i=0; i<pocet; i++) {
                            if (selectionInDataSpace.contains(point_x[lines_1[i]], point_y[lines_1[i]]) &&
                                selectionInDataSpace.contains(point_x[lines_2[i]], point_y[lines_2[i]])) {
                                selCount++;
                            }     
                        }
                        
                        System.out.println("lines in selection counted = " + selCount);
                        int selection[] = new int[selCount];
                        int selPointer = 0;
                        for (int i=0; i<pocet; i++) {
                            if (selectionInDataSpace.contains(point_x[lines_1[i]], point_y[lines_1[i]]) &&
                                selectionInDataSpace.contains(point_x[lines_2[i]], point_y[lines_2[i]])) {
                                selection[selPointer++] = i; 
                            }     
                        } 
                        System.out.println("Selection lines acquired");
                        p.setSelection(selection);
                        System.out.println("selection finished");
                    }
                }
                selectionRectangle = null;
                repaint();
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                requestFocusInWindow();
            }

        });
        addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                panelMouseDragged(e);
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                panelMouseMoved(e);
            }

        });
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
//                System.out.println("Key presse - code = " + e.getKeyCode() + "(code for del = "+KeyEvent.VK_DELETE+")");
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    if (plt != null && plt.getPltData() != null && highlightedLine != -1) {
                        for (PLTdata p: plt.getPltData()) {
                            if (p.getPen() == highlightedPen) {
                                p.deleteLine(highlightedLine);
                                p.calculateStats();
                                highlightedLine = -1;
                                highlightedPen = -1;
                                repaint();
                                break;
                            }
                        }
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_S) {
                    if (plt != null && plt.getPltData() != null) {
                        for (PLTdata p: plt.getPltData()) {
                            if (p.hasSelection()) {
                                p.makeSubPlotFromSelection();
                            }
                        }
                        repaint();
                   }
                } else if (e.getKeyCode() == KeyEvent.VK_L) {
                    if (plt != null && plt.getPltData() != null) {
                        for (PLTdata p: plt.getPltData()) {
                            p.makeSubplotsFromLoops();
                        }
                        repaint();
                   }                    
                } else if (e.getKeyCode() == KeyEvent.VK_ADD) {
                    setNewScale(scale*1.2, getWidth()/2, getHeight()/2);
                } else if (e.getKeyCode() == KeyEvent.VK_SUBTRACT) {
                    setNewScale(scale*0.85, getWidth()/2, getHeight()/2);
                }
            }
        });

        setFocusable(true);
        
        setTransferHandler( new TransferHandler() {
            @Override
            public boolean canImport(TransferHandler.TransferSupport support) {
//                System.out.println("canImport : isDrop " + support.isDrop());
//                for (DataFlavor df: support.getDataFlavors()) {
//                    System.out.println("flavor "+ df.getHumanPresentableName() +"; " + df.toString());
//                }
//                System.out.println("\tfileListFlavor\t= " + support.isDataFlavorSupported(DataFlavor.javaFileListFlavor));
//                System.out.println("\timageFlavor\t= " + support.isDataFlavorSupported(DataFlavor.imageFlavor));
//                System.out.println("\tstringFlavor\t= " + support.isDataFlavorSupported(DataFlavor.stringFlavor));
//                System.out.println("\tplainTextFlavor\t= " + support.isDataFlavorSupported(DataFlavor.plainTextFlavor));

                if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    return false;
                }

                boolean copySupported = (COPY & support.getSourceDropActions()) == COPY;
//                System.out.println("\tcopySupported " + copySupported);

                if (!copySupported) {
                    return false;
                }

                support.setDropAction(COPY);

                return true;
            }

            @Override
            public boolean importData(TransferHandler.TransferSupport support) {
                System.out.println("Import data:");
                if (!canImport(support)) {
                    return false;
                }

                Transferable t = support.getTransferable();
//                DataFlavor myDf = new DataFlavor(Reader.class,"text/plain");
//                for (DataFlavor df: support.getDataFlavors()) {
//                    if (df.isFlavorTextType() && df.isRepresentationClassReader()) {
//                        myDf = df;
//                    }
//                }

                try {
                    String filePath = (String) t.getTransferData(DataFlavor.stringFlavor);
                    System.out.println(filePath);
                    
//                    BufferedReader br = new BufferedReader(myDf.getReaderForText(t));
//                    String text = "";
//                    while ((text = br.readLine()) != null) {
//                        System.out.println(text);            
//                    }
                    
                    URI uri = new URI(filePath);
                    File f = new File(uri);
                    System.out.println("file set to URI " + uri + " and starting to read file");
                    plt.readPLTfromFile(f);
                    System.out.println("file read, now repainting");
                    setAutoScaleAndCenter();           
                    repaint();
                    System.out.println("finished");
                } catch (URISyntaxException ex) {
                    System.out.println(ex);
                    Logger.getLogger(PLTpanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnsupportedFlavorException e) {
                    System.out.println(e);
                    return false;
                } catch (IOException e) {
                    System.out.println(e);
                    return false;
                } catch (Exception e) {
                    System.out.println(e);
                    return false;
                }

                return true;
            }
        }
        );
    }
    
    private void paintPlot(Graphics2D g, PLTdata p) {
        int lines_1[] = p.getLines_1();
        int lines_2[] = p.getLines_2();
        int lineType[] = p.getLineType();
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
        
        PLTdata subPlots[] = p.getSubPlots();
        for (PLTdata subPlot: subPlots) {
            g.setColor(Color.cyan);
            Rectangle bb = subPlot.getBoundingBox();
            g.fillRect(transformX(bb.getX()),
                       transformY(bb.getY() + bb.getHeight()),
                       transformX(bb.getX() + bb.getWidth()) - transformX(bb.getX()),
                       -transformY(bb.getY() + bb.getHeight()) + transformY(bb.getY()));
            paintPlot(g, subPlot);
        }              
        
        for (int i = 0; i < pocet; i++) {
            if (kreslitPrejezdy && (lines_1[i] != lastPoint)) {
                g.setColor(getColorForPen(-1));
                g.setStroke(new BasicStroke(1));
                g.drawLine(transformX(lastX), transformY(lastY),
                           transformX(point_x[lines_1[i]]),transformY(point_y[lines_1[i]]));                    
//                    if (i <= 5) {
//                        System.out.println("ir="+i+"; x1="+transformX(x2[i-1])+";y1="+transformY(y2[i-1])
//                               +";x2="+transformX(x1[i])+";y2="+transformY(y1[i]));
//                    }
            }

            if (p.isInSelection(i)) {
                g.setColor(new Color(128,255,128));
                g.setStroke(new BasicStroke(3));
            } else {
                if (kreslitStatus) {
                    g.setColor(getColorForStatus(status[i]));
                } else {
                    g.setColor(getColorForPen(p.getPen()));
                }

                g.setStroke(new BasicStroke(1));
            }

            if (i==highlightedLine && highlightedPen == p.getPen()) {
                g.setStroke(new BasicStroke(3));
            }

            if (lineType[i] < 10) {
                g.drawLine(transformX(point_x[lines_1[i]]), transformY(point_y[lines_1[i]]),
                           transformX(point_x[lines_2[i]]), transformY(point_y[lines_2[i]])); 
            }

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
            for (int i=0; i< p.getPointsCount(); i++) {
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
    
    @Override
    protected void paintComponent(Graphics g_orig) {
        super.paintComponent(g_orig);
        Graphics2D g = (Graphics2D) g_orig;
        if (plt != null && plt.getPltData() != null) {
            // draw the background for area for output of plot
            g.setColor(Color.WHITE);
            g.fillRect(transformX(0), transformY(maxY), transformX(maxX) - transformX(0), transformY(0) - transformY(maxY));
//            System.out.println("drawing; scale = " + scale + "maxX = " + plt.getBoundingBox().getMaxX());
            for (PLTdata p: plt.getPltData()) {
                paintPlot(g, p);
            }
               
            if (displayScale) {
                double baseUnit = Math.pow(10, Math.floor(Math.log10(getWidth() / 2 / scale / 40.0)));
                int baseUnitInPixels = (int) (baseUnit*40*scale);
                int segmentsCount = (int) Math.min(Math.floor(getWidth() / baseUnitInPixels),10); 
                int barLength = (int) (segmentsCount * baseUnitInPixels); //(int) (getWidth() * baseUnit / scale);
                int barHeight = 15;
//                    System.out.println("base unit = " + baseUnit + "; bar length = " + barLength + "; panel width = " + getWidth() + "; scale = " + scale);
                g.setColor(Color.WHITE);
                g.fillRect(5, 5, barLength, barHeight);
                g.setColor(Color.BLACK);
                g.drawRect(5, 5, barLength, barHeight);
                for (int i = 0; i<segmentsCount; i++) {
                    if ((i % 2) == 0) {
                        g.fillRect(5+i*baseUnitInPixels, 5, baseUnitInPixels, barHeight);
                    }
                    g.drawString(Double.toString(baseUnit * (i+1)), 5 + (i+1)*baseUnitInPixels, barHeight + 5 +2+15);                        
                }
            }

            if (selectionRectangle != null) {
                //System.out.println("drawing selection from [" + selectionRectangle.x + "," + selectionRectangle.y + "] with dimensions ["+ selectionRectangle.width + "," + selectionRectangle.height);
                g.setXORMode(new Color(64,32,0));
                g.fillRect(selectionRectangle.x, selectionRectangle.y, selectionRectangle.width, selectionRectangle.height);
                g.setPaintMode();
            }                
        }
    }

    private int transformX(double x) {
        return (int) (getWidth()/2 + ((x-centerX) * scale));
    }

    private double backTransformX(int x) {
        return (double) ((x - getWidth()/2) / scale + centerX);
    }
    
    private int transformY(double y) {
        return (int) (getHeight()/2 - ((y-centerY) * scale));
    }

    private double backTransformY(int y) {
        return (double) ((getHeight()/2 - y) / scale + centerY);
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

    public void setPlt(PLTfile plt) {
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
        setNewScale(scale *= (1 - e.getWheelRotation() * 0.1), e.getX(), e.getY());
    }
    
    private void setNewScale(double newScale, int newCenterX, int newCenterY) {
        double origScale = scale;
        double origCenterX = transformX(centerX);
        double origCenterY = transformY(centerY);
        scale = newScale;
        centerX -= ((newCenterX - origCenterX) *(origScale - scale) / (scale * origScale));
        centerY += ((newCenterY - origCenterY) *(origScale - scale) / (scale * origScale));
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
        if (plt != null && plt.getPltData() != null) {
            for(PLTdata p: plt.getPltData()) {
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
        if (panMode) {
            centerX -= (e.getX() - dragPoint.x) / scale;
            centerY += (e.getY() - dragPoint.y) / scale;
            dragPoint.setLocation(e.getPoint());
            repaint();
        }
        
        if (selectMode) {
            selectionRectangle = new Rectangle(startSelectionPoint);
            selectionRectangle.add(e.getPoint());
            repaint();
        }
    }

    private void panelMouseMoved(MouseEvent e) {
        if (plt != null && plt.getPltData() != null) {
            highlightedLine = -1;
            highlightedPen = -1;
            boolean haveSelection = false;
            final int distanceThreshold = 7; // 7 pixels
            double distance = 0;
            for (PLTdata p: plt.getPltData()) {
                int lines_1[] = p.getLines_1();
                int lines_2[] = p.getLines_2();
                int point_x[] = p.getPoint_x();
                int point_y[] = p.getPoint_y();
                int pocet = p.getPopulatedLines();        

                int p1x, p1y, p2x, p2y, mx, my;

                for (int i = 0; i < pocet; i++) {
                    p1x = transformX(point_x[lines_1[i]]);
                    p1y = transformY(point_y[lines_1[i]]);
                    p2x = transformX(point_x[lines_2[i]]);
                    p2y = transformY(point_y[lines_2[i]]);

                    // skip all lines that are not visible
                    if ((p1x < 0 && p2x < 0)
                        || (p1x > getWidth() && p2x > getWidth())
                        || (p1y < 0 && p2y < 0)
                        || (p1y > getHeight()) && p2y > getHeight()) {
                        continue;
                    }
                    
                    // skipt lines, in whose bounding box the mouse is not located
                    Rectangle r = new Rectangle(new Point(p1x,p1y));
                    r.add(p2x,p2y);
                    r.grow(distanceThreshold,distanceThreshold);
                    if (!r.contains(e.getPoint())) {
                        continue;
                    }

                    // for visible lines calculate distance to mouse point
                    mx = e.getX();
                    my = e.getY();

                    int a = p2y - p1y;
                    int b = -(p2x - p1x);
                    float c = -p1x*a - p1y*b;

                    distance = (Math.abs(a*mx + b*my + c) / Math.sqrt(a*a + b*b));

                    if (distance <= distanceThreshold) {
                        haveSelection = true;
                        highlightedLine = i;
                        highlightedPen = p.getPen();
                        break;
                    }                 
                }
                if (haveSelection) {
                    break;
                }
            }
            repaint();
        }
    }

    public void setSelectMode() {
        this.selectMode = true;
        this.panMode = false;
    }

    public void setPanMode() {
        this.panMode = true;
        this.selectMode = false;
    }
    
    
}
