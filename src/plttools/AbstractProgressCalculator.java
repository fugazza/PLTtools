/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plttools;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 *
 * @author ajgl
 */
public abstract class AbstractProgressCalculator implements PropertyChangeListener {

    protected PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);
    protected String progressMessage;
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public abstract int getMax();
    
    public abstract int getProgress();

    public void setMessage(String progressMessage) {
        String oldMessage = this.progressMessage;
        this.progressMessage = progressMessage;
        String newMessage = this.progressMessage;
        //System.out.println("progresss calculator " + this.toString() + ": Firing progressMessage: old message = " + oldMessage + "; new message = " + newMessage);
        propertySupport.firePropertyChange("progressMessage", oldMessage, newMessage);
    }

    public void finishedAnnouncement() {
        propertySupport.firePropertyChange("progressFinished", false, true);        
    }
    
    public String getMessage() {
        return progressMessage;
    }    
    
    public int progressPercentage() {
        int max = this.getMax();
        int pro = this.getProgress();
        int p = (pro>max) ? max : pro;
        int result = (int) ((100.0*p)/max);
        //System.out.println("progress calculation " + this.toString() + " progress = " + p + " from max = " + max + " = " + result + "%");
        return result;
    }
}
