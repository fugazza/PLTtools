/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package plttools;

/**
 *
 * @author vlada
 */
public class SettingsData {

    private boolean correctorMoveToOrigin;
    private int correctorOffsetX;
    private int correctorOffsetY;
    private boolean correctorMergeIdentic;
    private float correctorTolerance;
    
    private int antCount;

    public void setCorrectorMoveToOrigin(boolean correctorMoveToOrigin) {
        this.correctorMoveToOrigin = correctorMoveToOrigin;
    }

    public boolean getCorrectorMoveToOrigin() {
        return correctorMoveToOrigin;
    }

    public int getCorrectorOffsetX() {
        return correctorOffsetX*40;
    }

    public void setCorrectorOffsetX(int correctorOffsetX) {
        this.correctorOffsetX = correctorOffsetX;
    }

    public int getCorrectorOffsetY() {
        return correctorOffsetY*40;
    }

    public void setCorrectorOffsetY(int correctorOffsetY) {
        this.correctorOffsetY = correctorOffsetY;
    }

    public float getCorrectorTolerance() {
        return correctorTolerance*40;
    }

    public void setCorrectorTolerance(float correctorTolerance) {
        this.correctorTolerance = correctorTolerance;
    }

    public boolean getCorrectorMergeIdentic() {
        return correctorMergeIdentic;
    }

    public void setCorrectorMergeIdentic(boolean correctorMergeIdentic) {
        this.correctorMergeIdentic = correctorMergeIdentic;
    }

    public int getAntCount() {
        return antCount;
    }

    public void setAntCount(int antCount) {
        this.antCount = antCount;
    }
    
}
