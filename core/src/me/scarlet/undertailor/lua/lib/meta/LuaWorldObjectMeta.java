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

package me.scarlet.undertailor.lua.lib.meta;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import me.scarlet.undertailor.collision.bbshapes.BoundingBox;
import me.scarlet.undertailor.collision.bbshapes.BoundingCircle;
import me.scarlet.undertailor.collision.bbshapes.BoundingRectangle;
import me.scarlet.undertailor.environment.overworld.WorldObject;
import me.scarlet.undertailor.gfx.AnimationData;
import me.scarlet.undertailor.lua.Lua;
import me.scarlet.undertailor.lua.LuaLibrary;
import me.scarlet.undertailor.lua.LuaLibraryComponent;
import me.scarlet.undertailor.lua.LuaObjectValue;
import me.scarlet.undertailor.lua.lib.game.AnimationLib;
import me.scarlet.undertailor.util.LuaUtil;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

public class LuaWorldObjectMeta extends LuaLibrary {
    
    public static LuaObjectValue<WorldObject> check(LuaValue value) {
        return LuaUtil.checkType(value, Lua.TYPENAME_WORLDOBJECT);
    }
    
    public static LuaObjectValue<WorldObject> create(WorldObject value) {
        return LuaObjectValue.of(value, Lua.TYPENAME_WORLDOBJECT, Lua.META_WORLDOBJECT);
    }
    
    public static final LuaLibraryComponent[] COMPONENTS = {
            new getID(),
            new updateCollision(),
            new getHeight(),
            new setHeight(),
            new getRotation(),
            new setRotation(),
            new isIgnoringCollisionWith(),
            new setIgnoringCollisionWith(),
            new isOneSidedReaction(),
            new setOneSidedReaction(),
            new getBodyType(),
            new setBodyType(),
            new getZ(),
            new setZ(),
            new getVelocity(),
            new setVelocity(),
            new getPosition(),
            new setPosition(),
            new getAnimation(),
            new setAnimation(),
            new createBoundingBox(),
            new removeBoundingBox(),
            new getBoundingBox(),
            new getScale(),
            new setScale(),
            new canCollide(),
            new setCanCollide(),
            new isVisible(),
            new setVisible(),
            new getRoom(),
            new destroy(),
            new isPersisting(),
            new setPersisting()
    };
    
    public LuaWorldObjectMeta() {
        super(null, COMPONENTS);
    }
    
    static class getID extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 1, 1);
            
            WorldObject object = check(args.arg1()).getObject();
            return LuaValue.valueOf(object.getId());
        }
    }
    
    static class getRotation extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 1, 1);
            
            WorldObject object = check(args.arg1()).getObject();
            return LuaValue.valueOf(object.getRotation());
        }
    }
    
    static class setRotation extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 2, 2);
            
            WorldObject object = check(args.arg1()).getObject();
            float rotation = new Float(args.checkdouble(2));
            object.setRotation(rotation);
            return LuaValue.NIL;
        }
    }
    
    static class isIgnoringCollisionWith extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 2, 2);

            WorldObject object = check(args.arg1()).getObject();
            WorldObject collider = check(args.arg(2)).getObject();
            
            return LuaValue.valueOf(object.isCollisionIgnored(collider));
        }
    }
    
    static class setIgnoringCollisionWith extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 3, 3);

            WorldObject object = check(args.arg1()).getObject();
            WorldObject collider = check(args.arg(2)).getObject();
            boolean flag = args.checkboolean(3);
            
            object.setIgnoreCollisionWith(collider, flag);
            return LuaValue.NIL;
        }
    }
    
    static class updateCollision extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 1, 1);
            
            WorldObject object = check(args.arg1()).getObject();
            object.updateCollision();
            
            return LuaValue.NIL;
        }
    }
    
    static class isOneSidedReaction extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 1, 1);
            
            WorldObject object = check(args.arg1()).getObject();
            return LuaValue.valueOf(object.isOneSidedReaction());
        }
    }
    
    static class setOneSidedReaction extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 2, 2);
            
            WorldObject object = check(args.arg1()).getObject();
            boolean flag = args.checkboolean(2);
            object.setOneSidedReaction(flag);
            return LuaValue.NIL;
        }
    }
    
    static class getBodyType extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 1, 1);
            
            WorldObject object = check(args.arg1()).getObject();
            BodyType type = null;
            if(object.getBody() == null) {
                type = object.getBodyDef().type;
            } else {
                type = object.getBody().getType();
            }
            
            switch(type) {
                case DynamicBody:
                    return LuaValue.valueOf(0);
                case KinematicBody:
                    return LuaValue.valueOf(1);
                case StaticBody:
                    return LuaValue.valueOf(2);
                default:
                    return LuaValue.valueOf(-1);
            }
        }
    }
    
    static class setBodyType extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 2, 2);
            
            WorldObject object = check(args.arg1()).getObject();
            int typeId = args.checkint(2);
            
            BodyType type = null;
            switch(typeId) {
                case 0:
                    type = BodyType.DynamicBody;
                    break;
                case 1:
                    type = BodyType.KinematicBody;
                    break;
                case 2:
                    type = BodyType.StaticBody;
                    break;
                default:
                    throw new LuaError("bad argument #2: invalid body type id (must be 0, 1, or 2)");
            }
            
            if(object.getBody() == null) {
                object.getBodyDef().type = type;
            } else {
                object.getBody().setType(type);
            }
            
            return LuaValue.NIL;
        }
    }
    
    static class createBoundingBox extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 2, 3);
            
            WorldObject object = check(args.arg1()).getObject();
            String boxId = args.checkjstring(2);
            int type = args.optint(3, 1);
            BoundingBox box;
            if(type == 1) { // rectangle
                box = new BoundingRectangle();
            } else {
                box = new BoundingCircle();
            }
            
            object.setBoundingBox(boxId, box);
            return LuaBoundingBoxMeta.create(box);
        }
    }
    
    static class removeBoundingBox extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 2, 2);
            
            WorldObject object = check(args.arg1()).getObject();
            String boxId = args.checkjstring(2);
            
            object.setBoundingBox(boxId, null);
            
            return LuaValue.NIL;
        }
    }
    
    static class getHeight extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 1, 1);
            
            WorldObject object = check(args.arg1()).getObject();
            return LuaValue.valueOf(object.getHeight());
        }
    }
    
    static class setHeight extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 2, 2);
            
            WorldObject object = check(args.arg1()).getObject();
            object.setHeight(new Float(args.checkdouble(2)));
            return LuaValue.NIL;
        }
    }
    
    static class getZ extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 1, 1);
            
            WorldObject object = check(args.arg1()).getObject();
            return LuaValue.valueOf(object.getZ());
        }
    }
    
    static class setZ extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 2, 2);
            
            WorldObject object = check(args.arg1()).getObject();
            int z = args.checkint(2);
            object.setZ(z);
            return LuaValue.NIL;
        }
    }
    
    static class getVelocity extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 1, 1);
            
            WorldObject object = check(args.arg1()).getObject();
            Vector2 vel = object.getBody().getLinearVelocity();
            return LuaValue.varargsOf(new LuaValue[] {
                    LuaValue.valueOf(vel.x),
                    LuaValue.valueOf(vel.y)});
        }
    }
    
    static class setVelocity extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 2, 4);
            
            WorldObject object = check(args.arg(1)).getObject();
            Vector2 vel = object.getBody().getLinearVelocity();
            float x = new Float(args.optdouble(2, vel.x));
            float y = new Float(args.optdouble(3, vel.y));
            int movetype = args.optint(4, 0);
            vel.set(x, y);
            
            switch(movetype) {
                case 1: // impulse
                    Vector2 pos = object.getBody().getPosition();
                    object.getBody().applyLinearImpulse(vel, pos, true);
                    break;
                case 2: // force
                    object.getBody().applyForceToCenter(vel, true);
                    break;
                default: // direct
                    object.getBody().setLinearVelocity(vel);
                    break;
            }
            
            return LuaValue.NIL;
        }
    }
    
    static class getPosition extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 1, 1);
            
            WorldObject object = check(args.arg1()).getObject();
            Vector2 pos = object.getPosition();
            return LuaValue.varargsOf(new LuaValue[] {
                    LuaValue.valueOf(pos.x),
                    LuaValue.valueOf(pos.y)});
        }
    }
    
    static class setPosition extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 2, 3);
            
            WorldObject object = check(args.arg1()).getObject();
            Vector2 pos = object.getPosition();
            float x = new Float(args.optdouble(2, pos.x));
            float y = new Float(args.optdouble(3, pos.y));
            
            object.setPosition(x, y);
            return LuaValue.NIL;
        }
    }
    
    static class getAnimation extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 2, 2);
            
            WorldObject object = check(args.arg1()).getObject();
            String animationId = args.checkjstring(2);
            AnimationData anim = object.getAnimation(animationId);
            if(anim == null) {
                return LuaValue.NIL;
            } else {
                return AnimationLib.create(anim);
            }
        }
    }
    
    static class setAnimation extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 3, 3);
            
            WorldObject object = check(args.arg(1)).getObject();
            String animationId = args.checkjstring(2);
            AnimationData anim = args.isnil(3) ? null : AnimationLib.check(args.arg(3)).getObject();
            object.setAnimation(animationId, anim);
            return LuaValue.NIL;
        }
    }
    
    static class getBoundingBox extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 2, 2);
            
            WorldObject object = check(args.arg1()).getObject();
            String boxId = args.checkjstring(2);
            return LuaBoundingBoxMeta.create(object.getBoundingBox(boxId));
        }
    }
    
    static class getScale extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 1, 1);
            
            WorldObject object = check(args.arg1()).getObject();
            return LuaValue.valueOf(object.getScale());
        }
    }
    
    static class setScale extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 2, 2);
            
            WorldObject object = check(args.arg1()).getObject();
            float scale = new Float(args.checkdouble(2));
            object.setScale(scale);
            return LuaValue.NIL;
        }
    }
    
    static class canCollide extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 1, 1);
            
            WorldObject object = check(args.arg1()).getObject();
            return LuaValue.valueOf(object.canCollide());
        }
    }
    
    static class setCanCollide extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 2, 2);
            
            WorldObject object = check(args.arg1()).getObject();
            boolean flag = args.checkboolean(2);
            object.setCanCollide(flag);
            return LuaValue.NIL;
        }
    }
    
    static class isVisible extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 1, 1);
            
            WorldObject object = check(args.arg1()).getObject();
            return LuaValue.valueOf(object.isVisible());
        }
    }
    
    static class setVisible extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 2, 2);
            
            WorldObject object = check(args.arg1()).getObject();
            boolean flag = args.checkboolean(2);
            object.setVisible(flag);
            return LuaValue.NIL;
        }
    }
    
    static class getRoom extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 1, 1);
            
            WorldObject object = check(args.arg1()).getObject();
            if(object.getRoom() != null) {
                return LuaWorldRoomMeta.create(object.getRoom());
            } else {
                return LuaValue.NIL;
            }
        }
    }
    
    static class destroy extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 1, 1);
            
            WorldObject object = check(args.arg1()).getObject();
            object.destroy();
            return LuaValue.NIL;
        }
    }
    
    static class isPersisting extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 1, 1);
            
            WorldObject object = check(args.arg1()).getObject();
            return LuaValue.valueOf(object.isPersisting());
        }
    }
    
    static class setPersisting extends LibraryFunction {
        @Override
        public Varargs execute(Varargs args) {
            LuaUtil.checkArguments(args, 2, 2);
            
            WorldObject object = check(args.arg1()).getObject();
            boolean flag = args.checkboolean(2);
            object.setPersisting(flag);
            return LuaValue.NIL;
        }
    }
}
