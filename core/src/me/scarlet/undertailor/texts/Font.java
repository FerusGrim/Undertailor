package me.scarlet.undertailor.texts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import me.scarlet.undertailor.exception.ConfigurationException;
import me.scarlet.undertailor.exception.TextureTilingException;
import me.scarlet.undertailor.gfx.Sprite;
import me.scarlet.undertailor.gfx.Sprite.SpriteMeta;
import me.scarlet.undertailor.gfx.SpriteSheet;
import me.scarlet.undertailor.gfx.SpriteSheet.SpriteSheetMeta;
import me.scarlet.undertailor.texts.TextComponent.DisplayMeta;
import me.scarlet.undertailor.texts.TextComponent.Text;
import me.scarlet.undertailor.util.ConfigurateUtil;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Font {
    
    public static class FontData {
        
        public static class CharMeta {
            
            private Integer offX, offY, boundWrapX, boundWrapY;
            
            public CharMeta() {
                this.setValues(new Integer[] {null, null, null, null});
            }
            
            public static CharMeta fromConfig(ConfigurationNode root) {
                try {
                    CharMeta meta = new CharMeta(ConfigurateUtil.processIntegerArray(root, new Integer[] {null, null, null, null}));
                    return meta;
                } catch(ConfigurationException e) {
                    throw e;
                }
            }
            
            public CharMeta(Integer[] values) {
                this.setValues(values);
            }
            
            public CharMeta(Integer offX, Integer offY) {
                this(offX, offY, null, null);
            }
            
            public CharMeta(Integer offX, Integer offY, Integer boundWrapX, Integer boundWrapY) {
                this.offX = offX;
                this.offY = offY;
                this.boundWrapX = boundWrapX;
                this.boundWrapY = boundWrapY;
            }
            
            public int getOffsetX() {
                return offX == null ? 0 : offX;
            }
            
            public int getOffsetY() {
                return offY == null ? 0 : offY;
            }
            
            public int getBoundWrapX() {
                return boundWrapX == null ? 0 : boundWrapX;
            }
            
            public int getBoundWrapY() {
                return boundWrapY == null ? 0 : boundWrapY;
            }
            
            public int[] values() {
                return new int[] {getOffsetX(), getOffsetY(), getBoundWrapX(), getBoundWrapY()};
            }
            
            public Integer[] rawValues() {
                return new Integer[] {offX, offY, boundWrapX, boundWrapY};
            }
            
            public void setValues(Integer[] values) {
                this.offX = values[0];
                this.offY = values[1];
                this.boundWrapX = values[2];
                this.boundWrapY = values[3];
            }
            
            public CharMeta merge(CharMeta otherMeta) {
                Integer[] values = otherMeta.rawValues();
                Integer[] set = new Integer[values.length];
                for(int i = 0; i < set.length; i++) {
                    if(values[i] != null) {
                        set[i] = values[i];
                    } else {
                        set[i] = this.values()[i];
                    }
                }
                
                return new CharMeta(set);
            }
            
            public SpriteMeta asSpriteMeta() {
                return new SpriteMeta(0, 0, getOffsetX(), getOffsetY(), getBoundWrapX(), getBoundWrapY());
            }
            
            @Override
            public String toString() {
                return "[" + getOffsetX() + ", " + getOffsetY() + ", " + getBoundWrapX() + ", " + getBoundWrapY() + "]";
            }
        }
        
        public static FontData fromConfig(String name, ConfigurationNode node) {
            FontData data = new FontData();
            ConfigurationNode root = node.getNode("font");
            try {
                data.fontName = name;
                data.x = ConfigurateUtil.processInt(root.getNode("gridSizeX"), null);
                data.y = ConfigurateUtil.processInt(root.getNode("gridSizeY"), null);
                data.space = ConfigurateUtil.processInt(root.getNode("spaceSize"), null);
                data.spacing = ConfigurateUtil.processInt(root.getNode("letterSpacing"), null);
                data.characterList = ConfigurateUtil.processString(root.getNode("charList"), null);
                
                if(!root.getNode("globalMeta").isVirtual()) {
                    data.globalMeta = CharMeta.fromConfig(root.getNode("globalMeta"));
                } else {
                    data.globalMeta = new CharMeta();
                }
                
                data.charMeta = new HashMap<>();
                for(Entry<Object, ? extends ConfigurationNode> entry: root.getNode("meta").getChildrenMap().entrySet()) {
                    if(data.charMeta.containsKey(entry.getKey())) {
                        data.charMeta.put(entry.getKey().toString(), data.charMeta.get(entry.getKey().toString()).merge(CharMeta.fromConfig(entry.getValue())));
                    } else {
                        data.charMeta.put(entry.getKey().toString(), CharMeta.fromConfig(entry.getValue()));
                    }
                }
                
                return data;
            } catch(RuntimeException e) {
                e.printStackTrace();
                return null;
            }
        }
        
        private String fontName;
        private CharMeta globalMeta;
        private String characterList;
        private Map<String, CharMeta> charMeta;
        private int x = 1, y = 1;
        private int spacing = -1; // pixels between letters
        private int space = -1; // pixels that count as a space
        
        private FontData() {}
        
        public String getName() {
            return fontName;
        }
        
        public CharMeta getFontMeta() {
            return globalMeta;
        }
        
        public CharMeta getCharacterMeta(char ch) {
            Set<CharMeta> collectedMeta = new HashSet<CharMeta>();
            for(Entry<String, CharMeta> entry : charMeta.entrySet()) {
                if(entry.getKey().indexOf(ch) != -1) {
                   collectedMeta.add(entry.getValue());
                }
            }
            
            if(!collectedMeta.isEmpty()) {
                CharMeta compiled = new CharMeta();
                for(CharMeta meta : collectedMeta) {
                    compiled = compiled.merge(meta);
                }
                
                return compiled;
            }
            
            return getFontMeta();
        }
        
        public int getLetterSpacing() {
            return spacing;
        }
        
        public int getSpaceSize() {
            return space;
        }
    }
    
    private FontData data;
    private SpriteSheet sheet;
    public Font(Texture spriteSheet, FontData data) throws TextureTilingException {
        this.data = data;
        SpriteSheetMeta sheetMeta = new SpriteSheetMeta();
        sheetMeta.gridX = data.x;
        sheetMeta.gridY = data.y;
        sheetMeta.spriteMeta = new SpriteMeta[data.characterList.length()];
        for(int i = 0; i < data.characterList.length(); i++) {
            sheetMeta.spriteMeta[i] = data.getCharacterMeta(data.characterList.charAt(i)).asSpriteMeta();
        }
        
        try {
            this.sheet = new SpriteSheet("font-" + data.fontName, spriteSheet, sheetMeta);
        } catch(TextureTilingException e) {
            throw e;
        }
    }
    
    public FontData getFontData() {
        return data;
    }
    
    public Sprite getChar(char ch) {
        return sheet.getSprite(data.characterList.indexOf(ch));
    }
    
    public void write(Batch batch, Text text, int posX, int posY) {
        write(batch, text.getText(), text.getStyle(), text.getColor(), posX, posY);
    }
    
    public void write(Batch batch, String text, Style style, Color color, int posX, int posY) {
        write(batch, text, style, color, posX, posY, 1);
    }
    
    public void write(Batch batch, String text, Style style, Color color, int posX, int posY, int scale) {
        write(batch, text, style, color, posX, posY, scale, 1.0F);
    }
    
    public void write(Batch batch, String text, Style style, Color color, int posX, int posY, int scale, float alpha) {
        if(text.trim().isEmpty()) {
            return;
        }
        
        char[] chars = new char[text.length()];
        text.getChars(0, text.length(), chars, 0);
        int pos = 0;
        
        //for(char chara : chars) {
        if(style != null) {
            style.onNextTextRender(Gdx.graphics.getDeltaTime());
        }
        
        for(int i = 0; i < chars.length; i++) {
            char chara = chars[i];
            if(Character.valueOf(' ').compareTo(chara) == 0) {
                pos += (this.getFontData().getSpaceSize() * scale);
                continue;
            }
            
            Sprite sprite = this.getChar(chara);
            Color used = color == null ? Color.WHITE : new Color(color);
            used.a = alpha;
            batch.setColor(used);
            float aX = 0F, aY = 0F, aScaleX = 1.0F, aScaleY = 1.0F;
            if(style != null) {
                DisplayMeta dmeta = style.applyCharacter(i, text.replaceAll(" ", "").length());
                if(dmeta != null) {
                    aX = dmeta.offX;
                    aY = dmeta.offY;
                    aScaleX = dmeta.scaleX;
                    aScaleY = dmeta.scaleY;
                }
            }
            
            float scaleX = scale * aScaleX;
            float scaleY = scale * aScaleY;
            float offsetX = aX * (scaleX);
            float offsetY = aY * (scaleY);
            float drawPosX = posX + pos + offsetX;
            float drawPosY = posY + offsetY;
            
            sprite.draw(batch, drawPosX, drawPosY, scaleX, scaleY, 0F, false, false, true);
            pos += ((sprite.getTextureRegion().getRegionWidth() + this.getFontData().getLetterSpacing()) * scaleX);
        }
    }
    
    public void sheetTest(Batch batch) {
        sheet.sheetTest(batch);
    }
    
    public void fontTest(Batch batch, int posX, int posY, int scale) {
        String charList = this.getFontData().characterList;
        batch.enableBlending();
        for(int i = 0; i < Math.ceil(charList.length()/13) + 1; i++) {
            int y = (i + 1) * 13;
            this.write(batch, charList.substring(i * 13, y > charList.length() ? charList.length() : y), null, null, posX, posY - (i * 15 * scale), scale);
        }
    }
}
