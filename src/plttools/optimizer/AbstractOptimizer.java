/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package plttools.optimizer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import plttools.PLTdata;
import plttools.ParentProgressCalculator;
import plttools.SettingsData;

/**
 *
 * @author vlada
 */
public abstract class AbstractOptimizer {
    
    protected ParentProgressCalculator progress;
            
    protected PLTdata pd;
    
    protected SettingsData settings;
    
    public abstract PLTdata optimize();

    public void setData(PLTdata pd) {
        this.pd = pd;
    }

    public void setSettings(SettingsData settings) {
        this.settings = settings;
    }
    
    public abstract boolean changesLineCount();

    public void setProgressCalculator(ParentProgressCalculator progress) {
        this.progress = progress;
        prepareProgressCalculators();
    }
   
    protected abstract void prepareProgressCalculators();
   
}
