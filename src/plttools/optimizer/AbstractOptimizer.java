/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package plttools.optimizer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import plttools.PLTdata;
import plttools.SettingsData;

/**
 *
 * @author vlada
 */
public abstract class AbstractOptimizer {

    
    protected PropertyChangeSupport propertySupport;
    
    protected PLTdata pd;
    
    protected SettingsData settings;
    
    public AbstractOptimizer() {
        propertySupport = new PropertyChangeSupport(this);
    }

    public abstract PLTdata optimize();

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void setData(PLTdata pd) {
        this.pd = pd;
    }

    public void setSettings(SettingsData settings) {
        this.settings = settings;
    }
    
    
}
