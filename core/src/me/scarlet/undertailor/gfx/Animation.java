package me.scarlet.undertailor.gfx;

import java.util.Map;

public abstract class Animation<T extends KeyFrame> {
    
    public static final String DEFAULT_SPRITESET = "default";
    
    private boolean loop;
    private long startTime;
    protected AnimationSet animSet;
    public Animation(long startTime, boolean loop) {
        this.startTime = startTime;
        this.animSet = null;
        this.animSet = null;
        this.loop = loop;
    }
    
    public boolean isLooping() {
        return this.loop;
    }
    
    public AnimationSet getParentSet() {
        return animSet;
    }
    
    public long getStartTime() {
        return this.startTime;
    }
    
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    public void drawCurrentFrame(long stateTime, float posX, float posY) {
        this.drawCurrentFrame(stateTime, posX, posY, 1F);
    }
    
    public void drawCurrentFrame(long stateTime, float posX, float posY, float scale) {
        this.drawCurrentFrame(stateTime, posX, posY, scale, 0F);
    }
    
    public abstract Map<Long, T> getFrames();
    public abstract T getFrame(long stateTime);
    public abstract void drawCurrentFrame(long stateTime, float posX, float posY, float scale, float rotation);
}