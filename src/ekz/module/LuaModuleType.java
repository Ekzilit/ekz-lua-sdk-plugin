package ekz.module;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class LuaModuleType extends ModuleType<LuaModuleBuilder> {
  public final static LuaModuleType INSTANCE = new LuaModuleType();

  private LuaModuleType() {
    super("LuaModule");
  }

  @NotNull
  @Override
  public LuaModuleBuilder createModuleBuilder() {
    return new LuaModuleBuilder();
  }

  @NotNull
  @Override
  public String getName() {
    return "Lua";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "Lua wow addon development support";
  }

  @Override
  public Icon getNodeIcon(boolean isOpened) {
    return AllIcons.Nodes.Module;
  }
}
