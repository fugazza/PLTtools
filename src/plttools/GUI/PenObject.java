/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package plttools.GUI;

import java.awt.Color;

/**
 *
 * @author vlada
 */
public class PenObject extends Object {
    
    private int num;
    
    public PenObject(int i) {
        num = i;
    }

    @Override
    public String toString() {
        if (num == -1) {
            return "all pens";
        } else {
            return "pen " + num + " ("+ getNameForColor(num) +")";
        }
    }

    public int getNum() {
        return num;
    }
    
    private String getNameForColor(int pen) {
        Color c = PLTpanel.getColorForPen(pen);
        if (c.equals(Color.RED)) {
            return "red";
        } else if (c.equals(Color.BLACK)) {
            return "black";
        } else if (c.equals(Color.YELLOW)) {
            return "yellow";
        } else if (c.equals(Color.GREEN)) {
            return "green";
        } else if (c.equals(Color.BLUE)) {
            return "blue";
        } else if (c.equals(Color.ORANGE)) {
            return "orange";
        } else {
            return "unknown color";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PenObject) {
            return ((PenObject) o).getNum() == num;
        } else {
            return false;
        }
    }
    
}
