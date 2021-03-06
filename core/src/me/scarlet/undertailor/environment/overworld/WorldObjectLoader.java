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

import me.scarlet.undertailor.Undertailor;
import me.scarlet.undertailor.exception.LuaScriptException;
import me.scarlet.undertailor.lua.LuaObjectValue;
import me.scarlet.undertailor.lua.impl.WorldObjectImplementable;
import me.scarlet.undertailor.manager.ScriptManager;
import me.scarlet.undertailor.util.LuaUtil;
import org.luaj.vm2.Varargs;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;

public class WorldObjectLoader {
    
    public static final String MANAGER_TAG = "worldobjectloader";
    
    private Map<String, File> map;
    
    public WorldObjectLoader() {
        this.map = new HashMap<>();
    }
    
    @SuppressWarnings("unchecked")
    public LuaObjectValue<WorldObject> newWorldObject(String objectName, Varargs args) {
        if(map.containsKey(objectName)) {
            try {
                ScriptManager scriptMan = Undertailor.getScriptManager();
                WorldObjectImplementable impl = scriptMan.getImplementable(WorldObjectImplementable.class);
                return (LuaObjectValue<WorldObject>) impl.load(objectName, map.get(objectName), args).getObjectValue();
            } catch(LuaScriptException e) {
                RuntimeException thrown = new RuntimeException("could not retrieve object " + objectName + ": " + LuaUtil.formatJavaException(e));
                thrown.initCause(e);
                throw thrown;
            }
        }
        
        Undertailor.instance.warn(MANAGER_TAG, "system requested non-existing object");
        return null;
    }
    
    public void loadObjects(File directory) {
        loadObjects(directory, null);
        Undertailor.instance.log(MANAGER_TAG, map.entrySet().size() + " object(s) currently loaded");
    }
    
    public void loadObjects(File directory, String heading) {
        if(heading == null) {
            heading = "";
        }
        
        String dirPath = directory.getAbsolutePath();
        if(!directory.exists()) {
            directory.mkdirs();
        }
        
        if(directory.isFile()) {
            Undertailor.instance.warn(MANAGER_TAG, "could not load objects from directory: " + dirPath + " (not a directory)");
            return;
        }
        
        Undertailor.instance.log(MANAGER_TAG, "loading objects scripts from directory " + dirPath);
        for(File file : directory.listFiles((FileFilter) (File file) -> {
            return file.getName().endsWith(".lua") || file.isDirectory();
        })) {
            if(file.isDirectory()) {
                loadObjects(file, heading.isEmpty() ? file.getName() : heading + "." + file.getName());
                continue;
            }
            
            String name = heading + (heading.isEmpty() ? "" : ".") + file.getName().split("\\.")[0];
            Undertailor.instance.debug(MANAGER_TAG, "registered object " + name);
            map.put(name, file);
        }
    }
}
