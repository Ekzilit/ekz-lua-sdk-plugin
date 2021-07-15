package ekz.openapi.options;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import ekz.LuaIcons;
import ekz.highlighter.LuaSyntaxHighlighter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class LuaColorSettingsPage implements ColorSettingsPage {
  private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
      new AttributesDescriptor("Key", LuaSyntaxHighlighter.KEY),
      new AttributesDescriptor("Operators", LuaSyntaxHighlighter.OPERATORS),
      new AttributesDescriptor("String", LuaSyntaxHighlighter.STRING),
      new AttributesDescriptor("Number", LuaSyntaxHighlighter.NUMBER),
      new AttributesDescriptor("Global var", LuaSyntaxHighlighter.GLOBAL_VAR),
      new AttributesDescriptor("Local var", LuaSyntaxHighlighter.LOCAL_VAR),
      new AttributesDescriptor("Function parameter", LuaSyntaxHighlighter.FUNC_PARAMS),
      new AttributesDescriptor("Line comment", LuaSyntaxHighlighter.LINE_COMMENT),
      new AttributesDescriptor("Block comment", LuaSyntaxHighlighter.BLOCK_COMMENT)
  };

  @Nullable
  @Override
  public Icon getIcon() {
    return LuaIcons.FILE;
  }

  @NotNull
  @Override
  public SyntaxHighlighter getHighlighter() {
    return new LuaSyntaxHighlighter();
  }

  @NotNull
  @Override
  public String getDemoText() {
    return "<globalVar>globalVar</globalVar> = 123\n" + "local <localVar>localVar</localVar> = \"string\"\n" +
        "-- line comment\n" + "--[[ block comment\n" + "     block comment\n" + "     block comment\n" +
        "     block comment ]]\n" + "<localVar>localVar</localVar> = funcCall(<funcParam>par1</funcParam>, " +
        "<funcParam>par2</funcParam>)\n";
  }

  @Nullable
  @Override
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    Map<String, TextAttributesKey> attributesKeyMap = new HashMap<>();
    attributesKeyMap.put("funcParam", LuaSyntaxHighlighter.FUNC_PARAMS);
    attributesKeyMap.put("globalVar", LuaSyntaxHighlighter.GLOBAL_VAR);
    attributesKeyMap.put("localVar", LuaSyntaxHighlighter.LOCAL_VAR);
    return attributesKeyMap;
  }

  @NotNull
  @Override
  public AttributesDescriptor[] getAttributeDescriptors() {
    return DESCRIPTORS;
  }

  @NotNull
  @Override
  public ColorDescriptor[] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "Lua";
  }
}
