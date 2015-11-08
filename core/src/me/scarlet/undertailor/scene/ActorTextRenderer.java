package me.scarlet.undertailor.scene;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import me.scarlet.undertailor.manager.FontManager;
import me.scarlet.undertailor.util.TextComponent.Text;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ActorTextRenderer extends Actor {
    
    public static class TextRendererMeta {
        
        public int x, y;
        public TextRendererMeta() {}
    }
    
    protected float alpha[];
    protected float toAlpha[];
    protected float alphaSpeed;
    protected boolean removeNext;
    protected TextRendererMeta meta;
    protected Map<Integer, Text> drawn;
    protected boolean visible, visibleText;
    
    public ActorTextRenderer(TextRendererMeta meta) {
        this.meta = meta;
        this.removeNext = false; // if true, we get advance the action on top of our stack
        this.drawn = new HashMap<>();
        this.visible = false;
        this.visibleText = true;
        this.alphaSpeed = 0.05F;
        
        this.alpha = new float[3];
        this.toAlpha = new float[3];
        
        this.setAlpha(1.0F);
        this.setTextAlpha(1.0F);
    }
    
    @Override
    public void act(float delta) {
        if(this.hasActions()) {
            ActionPlayText act = (ActionPlayText) this.getActions().get(0);
            boolean finished = act.act(delta);
            if(act.isControlled()) {
                if(act.isWaiting()) {
                    this.removeAction(act);
                } else {
                    if(this.removeNext) {
                        this.removeNext = false;
                        act.skip();
                    }
                }
            } else {
                if(finished) {
                    this.removeAction(act);
                }
            }
        }
    }
    
    public Collection<Text> getCurrentText() {
        return this.drawn.values();
    }
    
    protected Map<Integer, Text> getDrawn() {
        return drawn;
    }
    
    public void setVisible(boolean flag) {
        this.visible = flag;
    }
    
    public void setAlpha(float alpha) {
        this.setAlpha(alpha, false);
    }
    
    public void setAlpha(float alpha, boolean smooth) {
        this.toAlpha[0] = alpha;
        if(!smooth) {
            this.alpha[0] = alpha;
        }
    }
    
    public void setTextVisible(boolean flag) {
        this.visibleText = flag;
    }
    
    public void setTextAlpha(float alpha) {
        this.setTextAlpha(alpha, false);
    }
    
    public void setTextAlpha(float alpha, boolean smooth) {
        this.toAlpha[1] = alpha;
        if(!smooth) {
            this.alpha[1] = alpha;
        }
    }
    
    protected void prepareAlphas() {
        for(int i = 0; i < alpha.length; i++) {
            if(alpha[i] != toAlpha[i]) {
                if(toAlpha[i] > alpha[i]) {
                    alpha[i] = alpha[i] + alphaSpeed > toAlpha[i] ? toAlpha[i] : alpha[i] + alphaSpeed;
                } else {
                    alpha[i] = alpha[i] - alphaSpeed < toAlpha[i] ? toAlpha[i] : alpha[i] - alphaSpeed;
                }
            }
        }
    }
    
    @Override
    public void draw(Batch batch, float parentAlpha) {
        prepareAlphas();
        if(visible) {
            if(visibleText) {
                if(this.getDrawn() != null) {
                    SpriteBatch sbatch = new SpriteBatch();
                    int x = meta.x;
                    int y = meta.y;
                    for(Entry<Integer, Text> entry : this.getDrawn().entrySet()) {
                        Text text = entry.getValue();
                        FontManager.write(sbatch, text.getFont(), text.getText(), x, y, 2, parentAlpha * alpha[0] * alpha[1], text.getColor());
                    }
                    
                    sbatch.dispose();
                }
            }
        }
    }
}