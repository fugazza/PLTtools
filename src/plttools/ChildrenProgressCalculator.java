/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plttools;

import java.beans.PropertyChangeEvent;

/**
 *
 * @author ajgl
 */
public class ChildrenProgressCalculator extends AbstractProgressCalculator {
    
    private int max;
    private int progress;
    private float scale = 1.0f;
    
    public void addProgress(int progressToAdd) {
        int oldProgress = getProgress();
        
        this.progress += progressToAdd;
        if (progress > max) {
            progress = max;
        }
        
        int newProgress = getProgress();
        propertySupport.firePropertyChange("progressValue", oldProgress, newProgress);   
    }

    public void setProgress(int progressToSet) {
        int oldProgress = getProgress();
        
        this.progress = progressToSet;
        if (progress > max) {
            progress = max;
        }
        
        int newProgress = getProgress();
        propertySupport.firePropertyChange("progressValue", oldProgress, newProgress);   
    }
    
    @Override
    public int getMax() {
        return (int) (max * scale);
    }

    public void setMax(int max) {
        int oldMax = getMax();
        this.max = max;
        int newMax = getMax();
        propertySupport.firePropertyChange("maxValue", oldMax, newMax);
    }
       
    @Override
    public int getProgress() {
        return (int) (progress * scale);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        throw new UnsupportedOperationException("ChildrenProgressCalculator does not receive property changes."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setScale(float scale) {
        this.scale = scale;
    }
    
}
