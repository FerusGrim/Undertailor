package me.scarlet.undertailor.lua;

import me.scarlet.undertailor.Undertailor;
import me.scarlet.undertailor.collision.Collider;
import me.scarlet.undertailor.exception.LuaScriptException;
import me.scarlet.undertailor.lua.lib.meta.LuaWorldObjectMeta;
import me.scarlet.undertailor.manager.StyleManager;
import me.scarlet.undertailor.overworld.WorldObject;
import me.scarlet.undertailor.overworld.WorldRoom;
import me.scarlet.undertailor.overworld.WorldRoom.Entrypoint;
import me.scarlet.undertailor.util.InputRetriever.InputData;
import me.scarlet.undertailor.util.LuaUtil;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

public class LuaWorldObject extends LuaTable {

    public static LuaValue METATABLE;
    public static final String TYPENAME = "tailor-worldobj";
    
    static {
        LuaWorldObjectMeta.prepareMetatable();
    }
    
    public static final String IMPLMETHOD_CREATE = "create";         // create(obj)
    public static final String IMPLMETHOD_PROCESS = "process";       // process(delta, input)
    public static final String IMPLMETHOD_ONPAUSE = "onPause";       // onPause()
    public static final String IMPLMETHOD_ONRESUME = "onResume";     // onResume()
    public static final String IMPLMETHOD_ONRENDER = "onRender";     // onRender()
    public static final String IMPLMETHOD_ONCOLLIDE = "onCollide";   // onCollide(object)
    public static final String IMPLMETHOD_ONPERSIST = "onPersist";   // onPersist(object)
    public static final String IMPLMETHOD_ONINTERACT = "onInteract"; // onInteract(object)
    
    public static final String[] REQUIRED_METHODS = {IMPLMETHOD_CREATE};
    public static final String[] METHODS = {IMPLMETHOD_CREATE, IMPLMETHOD_PROCESS, IMPLMETHOD_ONRENDER, IMPLMETHOD_ONCOLLIDE, IMPLMETHOD_ONINTERACT, IMPLMETHOD_ONPERSIST, IMPLMETHOD_ONPAUSE, IMPLMETHOD_ONRESUME};
    
    public static LuaWorldObject checkWorldObject(LuaValue value) {
        if(!value.typename().equals(TYPENAME)) {
            throw new LuaError("bad argument: expected " + TYPENAME + ", got " + value.typename());
        }
        
        return (LuaWorldObject) value;
    }
    
    public static class LuaWorldObjectImpl extends WorldObject {
        
        private String typename;
        private Map<String, LuaFunction> functions;
        public LuaWorldObjectImpl(LuaWorldObject parent, File luaScript) throws LuaScriptException {
            this.typename = luaScript.getName().split("\\.")[0];
            Globals globals = Undertailor.newGlobals();
            try {
                LuaUtil.loadFile(globals, luaScript);
            } catch(FileNotFoundException e) {
                Undertailor.instance.error(StyleManager.MANAGER_TAG, "failed to load style: file " + luaScript.getAbsolutePath() + " wasn't found");
            }
            
            functions = LuaUtil.checkImplementation(globals, luaScript, REQUIRED_METHODS);
        }
        
        public Map<String, LuaFunction> getFunctions() {
            return functions;
        }
        
        @Override
        public void process(float delta, InputData input) {
            if(functions.containsKey(IMPLMETHOD_PROCESS)) {
                functions.get(IMPLMETHOD_PROCESS).call(LuaValue.valueOf(delta), new LuaInputData(input));
            }
        }
        
        @Override
        public void onRender() {
            if(functions.containsKey(IMPLMETHOD_ONRENDER)) {
                functions.get(IMPLMETHOD_ONRENDER).call();
            }
        }
        
        @Override
        public String getObjectName() {
            return typename;
        }

        @Override
        public void onCollide(Collider collider) {
            if(collider instanceof WorldObject) {
                if(functions.containsKey(IMPLMETHOD_ONCOLLIDE)) {
                    functions.get(IMPLMETHOD_ONCOLLIDE).call(new LuaWorldObject((WorldObject) collider));
                }
            }
        }
        
        @Override
        public void onPersist(WorldRoom newRoom, Entrypoint entrypoint) {
            if(functions.containsKey(IMPLMETHOD_ONPERSIST)) {
                functions.get(IMPLMETHOD_ONPERSIST).call(new LuaWorldRoom(newRoom), entrypoint == null ? LuaValue.NIL : new LuaEntrypoint(entrypoint));
            }
        }
        
        @Override
        public void onPause() {
            if(functions.containsKey(IMPLMETHOD_ONPAUSE)) {
                functions.get(IMPLMETHOD_ONPAUSE).call();
            }
        }
        
        @Override
        public void onResume() {
            if(functions.containsKey(IMPLMETHOD_ONRESUME)) {
                functions.get(IMPLMETHOD_ONRESUME).call();
            }
        }
    }
    
    private WorldObject obj;
    public LuaWorldObject(WorldObject obj) {
        this.setmetatable(METATABLE);
        this.obj = obj;
        prepareWorldObject();
    }
    
    public LuaWorldObject(File luaScript) throws LuaScriptException {
        this.setmetatable(METATABLE);
        this.obj = new LuaWorldObjectImpl(this, luaScript);
        ((LuaWorldObjectImpl) this.obj).getFunctions().get(IMPLMETHOD_CREATE).call(this);
        prepareWorldObject();
    }
    
    private void prepareWorldObject() {
        if(this.obj instanceof LuaWorldObjectImpl) {
            Map<String, LuaFunction> functions = ((LuaWorldObjectImpl) this.obj).getFunctions();
            for(Map.Entry<String, LuaFunction> entry : functions.entrySet()) {
                this.set(entry.getKey(), entry.getValue());
            }
        }
    }
    
    public WorldObject getWorldObject() {
        return obj;
    }
    
    @Override
    public void rawset(LuaValue key, LuaValue value) {
        super.rawset(key, value);
        if(value != LuaValue.NIL && this.obj instanceof LuaWorldObjectImpl) {
            LuaWorldObjectImpl luacom = (LuaWorldObjectImpl) this.obj;
            if(key.isstring()) {
                for(String method : METHODS) {
                    if(key.tojstring().equals(method)) {
                        luacom.getFunctions().put(method, value.checkfunction());
                        return;
                    }
                }
            }
        }
    }
    
    @Override
    public String typename() {
        return TYPENAME;
    }
}