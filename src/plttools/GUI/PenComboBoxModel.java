/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package plttools.GUI;

import javax.swing.AbstractListModel;
import javax.swing.MutableComboBoxModel;

/**
 *
 * @author vlada
 */
public class PenComboBoxModel extends AbstractListModel implements MutableComboBoxModel {

    private PenObject pens[] = new PenObject[1];
    private int selectedIndex;

    public PenComboBoxModel() {
        pens[0] = new PenObject(-1);
        selectedIndex = 0;
    }
    
    @Override
    public int getSize() {
//        System.out.println("getSize invoked - "+pens.length);
        return pens.length;
    }

    @Override
    public PenObject getElementAt(int i) {
//        System.out.println("required element #" + i + " = "+pens[i]);
        return pens[i];
    }

    @Override
    public void addElement(Object o) {
        PenObject p[] = new PenObject[pens.length+1];
        System.arraycopy(pens, 0, p, 0, pens.length);
        p[pens.length] = new PenObject(((Byte) o).byteValue());
//        System.out.println("added element "+ p[pens.length] + " to position " + pens.length );
        pens = p;
    }

    @Override
    public void removeElement(Object o) {
//        System.out.println("removing element "+ ((PenObject) o) );
        PenObject p[] = new PenObject[pens.length-1];
        int offset = 0;
        for(int i=0; i<pens.length; i++) {
            if (((PenObject) o).getNum() == pens[i].getNum()) {
                offset = -1;
            } else {
                p[i+offset] = pens[i];
            }
        }
        pens = p;
    }

    @Override
    public void insertElementAt(Object o, int i) {
        PenObject p[] = new PenObject[pens.length+1];
        int offset = 0;
        for(int j=0; j<pens.length; j++) {
            if (j == i) {
                p[i] = new PenObject(((Integer) o).intValue());
                offset = 1;
            } else {
                p[i] = pens[i+offset];
            }
        }
        pens = p;
    }

    @Override
    public void removeElementAt(int i) {
        PenObject p[] = new PenObject[pens.length-1];
        int offset = 0;
        for(int j=0; j<pens.length; j++) {
            if (j==i) {
                offset = -1;
            } else {
                p[i+offset] = pens[i];
            }
        }
        pens = p;
    }

    @Override
    public void setSelectedItem(Object o) {
        selectedIndex = 0;
        for (int i=0; i<pens.length; i++) {
            if (((PenObject) o).equals(pens[i])) {
                selectedIndex = i;
            }
        }
    }

    @Override
    public PenObject getSelectedItem() {
        if (selectedIndex>=0 && selectedIndex<pens.length) {
            return pens[selectedIndex];
        } else {
            return null;
        }        
    }    

}
