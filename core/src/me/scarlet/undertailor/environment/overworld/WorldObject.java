/* 
 * The MIT License (MIT)
 * 
 * Copyright (c) 2016 Tellerva, Marc Lawrence
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.scarlet.undertailor.environment.overworld;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import me.scarlet.undertailor.collision.Collider;
import me.scarlet.undertailor.collision.bbshapes.BoundingBox;
import me.scarlet.undertailor.environment.overworld.WorldRoom.Entrypoint;
import me.scarlet.undertailor.gfx.AnimationData;
import me.scarlet.undertailor.util.InputRetriever.InputData;
import me.scarlet.undertailor.util.Layerable;
import me.scarlet.undertailor.util.Positionable;
import me.scarlet.undertailor.util.Renderable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public abstract class WorldObject implements Collider, Layerable, Renderable, Positionable {
    
    public static BodyDef generateDefaultObjectDef() {
        BodyDef def = new BodyDef();
        
        def.active = true;
        def.awake = true;
        def.allowSleep = false;
        def.type = BodyDef.BodyType.DynamicBody;
        def.fixedRotation = true;
        def.position.set(0, 0);
        
        return def;
    }
    
    private int z;
    private float scale;
    private boolean persists;
    private boolean isVisible;
    
    //private AnimationData animation;
    private float height;
    private Set<Collider> contacts;
    private BodyDef bodyDef;
    private boolean canCollide;
    private boolean oneSided;
    
    private Map<String, AnimationData> animations;
    
    private Map<String, BoundingBox> boundingBoxes;
    private Map<Collider, String> ignoreCollideList;
    
    protected long id;
    protected Body body;
    protected WorldRoom room;
    
    public WorldObject() {
        this.z = 1;
        this.scale = 1F;
        this.height = 0F;
        this.room = null;
        this.isVisible = true;
        this.oneSided = false;
        this.canCollide = true;
        this.contacts = new HashSet<>();
        this.animations = new HashMap<>();
        this.boundingBoxes = new HashMap<>();
        
        this.bodyDef = WorldObject.generateDefaultObjectDef();
        this.ignoreCollideList = new WeakHashMap<>();
    }
    
    public BodyDef getBodyDef() {
        return this.bodyDef;
    }
    
    public void claim(long id, WorldRoom room, Body body) {
        this.id = id;
        this.room = room;
        this.body = body;
        this.body.setUserData(this);
        this.updateCollision();
    }
    
    @Override
    public boolean isCollisionIgnored(Collider obj) {
        return this.ignoreCollideList.containsKey(obj);
    }
    
    @Override
    public void setIgnoreCollisionWith(Collider obj, boolean flag) {
        if(isCollisionIgnored(obj) != flag) {
            if(flag) {
                this.ignoreCollideList.put(obj, "we don't care what this is");
            } else {
                this.ignoreCollideList.remove(obj);
            }
        }
    }
    
    public float getRotation() {
        if(this.body == null) {
            return new Float(Math.toDegrees(this.bodyDef.angle));
        }
        
        return new Float(Math.toDegrees(this.body.getAngle()));
    }
    
    public void setRotation(float rotation) {
        if(body == null) {
            this.bodyDef.angle = (float) Math.toRadians(rotation);
        } else {
            this.body.setTransform(body.getPosition(), (float) Math.toRadians(rotation));
        }
    }
    
    @Override
    public int getZ() {
        return z;
    }
    
    @Override
    public void setZ(int z) {
        this.z = z;
    }
    
    @Override
    public int getPriority() {
        return 1;
    }
    
    public long getId() {
        return id;
    }
    
    public float getScale() {
        return scale;
    }
    
    public void setScale(float scale) {
        this.scale = scale < 0F ? 0F : scale;
        for(BoundingBox box : boundingBoxes.values()) {
            box.setScale(this.scale);
        }
    }
    
    @Override
    public BoundingBox getBoundingBox(String id) {
        return this.boundingBoxes.get(id);
    }
    
    @Override
    public void setBoundingBox(String id, BoundingBox box) {
        if(box == null && boundingBoxes.containsKey(id)) {
            this.boundingBoxes.get(id).destroyFixture(body);
            this.boundingBoxes.remove(id);
        } else {
            this.boundingBoxes.put(id, box);
        }
    }
    
    @Override
    public boolean isOneSidedReaction() {
        return this.oneSided;
    }
    
    @Override
    public void setOneSidedReaction(boolean flag) {
        this.oneSided = flag;
    }
    
    public float getHeight() {
        return height;
    }
    
    public void setHeight(float height) {
        this.height = height;
    }
    
    public boolean isPersisting() {
        return persists;
    }
    
    public void setPersisting(boolean flag) {
        this.persists = flag;
    }
    
    @Override
    public boolean canCollide() {
        return this.canCollide;
    }
    
    public void setCanCollide(boolean flag) {
        this.canCollide = flag;
        if(this.body == null) {
            this.bodyDef.active = flag;
        }
    }
    
    public boolean isVisible() {
        return isVisible();
    }
    
    public void setVisible(boolean flag) {
        this.isVisible = flag;
    }
    
    public Vector2 getPosition() {
        if(this.body == null) {
            return this.bodyDef.position;
        }
        
        return this.body.getPosition();
    }
    
    public void setPosition(float x, float y) {
        if(this.body == null) {
            this.bodyDef.position.set(x, y);
        } else {
            this.body.setTransform(x, y, this.body.getAngle());
        }
    }
    
    public WorldRoom getRoom() {
        return room;
    }
    
    public void destroy() {
        room.removeObject(id);
    }
    
    public AnimationData getAnimation(String id) {
        return this.animations.get(id);
    }
    
    public void setAnimation(String id, AnimationData data) {
        if(data == null) {
            if(this.animations.containsKey(id)) this.animations.remove(id);
        } else {
            this.animations.put(id, data);
        }
    }
    
    @Override
    public Set<BoundingBox> getBoundingBoxes() {
        return new HashSet<>(this.boundingBoxes.values());
    }
    
    @Override
    public Body getBody() {
        return body;
    }
    
    @Override
    public Set<Collider> getContacts() {
        return this.contacts;
    }
    
    public void render() {
        onRender();
        if(isVisible) {
            for(AnimationData animation : this.animations.values()) {
                animation.drawCurrentFrame(body.getPosition().x, body.getPosition().y + height, scale, (float) Math.toDegrees(body.getAngle()));
            }
        }
    }
    
    public void onPause() {}
    public void onResume() {}
    public void onRender() {}
    public void onDestroy() {}
    public void onRegister(long id, WorldRoom room) {}
    public void onPersist(WorldRoom newRoom, Entrypoint entrypoint) {}
    
    public void process(float delta, InputData input) {
        if(this.body != null && this.body.isActive() != this.canCollide) {
            this.body.setActive(this.canCollide);
        }
    }
    
    @Override public void onCollide(Collider collider) {}
    public abstract String getObjectName();
}
