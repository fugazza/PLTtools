/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plttools;

import java.beans.PropertyChangeEvent;
import java.util.Vector;

/**
 *
 * @author ajgl
 */
public class ParentProgressCalculator extends AbstractProgressCalculator{
    
    private Vector<AbstractProgressCalculator> childrenProgressCalculators = new Vector<AbstractProgressCalculator>();
    
    private int max;
    private int progress;
    
    public void register(AbstractProgressCalculator children) {
        //System.out.println("parent progresss calculator " + this.toString() + ": registering children " + children.toString());
        childrenProgressCalculators.add(children);
        children.addPropertyChangeListener(this);  
        int oldMax = max;
        recalculateMax();
        int newMax = max;
        propertySupport.firePropertyChange("maxValue", oldMax, newMax);
    }
    
    public void unregisterAll() {
        childrenProgressCalculators.clear();
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "progressValue":
                int oldProgress = progress;
                recalculateProgress();
                int newProgress = progress;
                //System.out.println("parent progresss calculator " + this.toString() + ": re-firing progressValue: old = " + oldProgress + "; new = " + newProgress);
                propertySupport.firePropertyChange("progressValue", oldProgress, newProgress);
                break;
            case "maxValue":
                int oldMax = max;
                recalculateMax();
                int newMax = max;
                //System.out.println("parent progresss calculator " + this.toString() + ": re-firing maxValue: old max = " + oldMax + "; new max = " + newMax);
                propertySupport.firePropertyChange("maxValue", oldMax, newMax);
                break;
            case "progressMessage":
                String oldMessage = this.progressMessage;
                this.progressMessage = (String) evt.getNewValue();
                String newMessage = this.progressMessage; 
                //System.out.println("parent progresss calculator " + this.toString() + ": re-firing progressMessage: old message = " + oldMessage + "; new message = " + newMessage);
                propertySupport.firePropertyChange("progressMessage", oldMessage, newMessage);
                break;
            case "progressFinished":
                propertySupport.firePropertyChange("progressFinished", false, true);
                break;
            default:
                break;
        }
    }
    
    private void recalculateMax() {
        max = 0;
        for (AbstractProgressCalculator children: childrenProgressCalculators) {
            max += children.getMax();            
        }        
    }

    private void recalculateProgress() {
        progress = 0;
        for (AbstractProgressCalculator children: childrenProgressCalculators) {
            progress += children.getProgress();            
        }        
    }
    
    @Override
    public int getMax() {
        //System.out.println("progress calculator (" + this.toString() + ") - max = " + max);
        return max;
    }

    @Override
    public int getProgress() {
        //System.out.println("progress calculator (" + this.toString() + ") - progress = " + progress);
        return progress;
    }
}
