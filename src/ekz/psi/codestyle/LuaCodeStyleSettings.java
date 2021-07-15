package ekz.psi.codestyle;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;

public class LuaCodeStyleSettings extends CustomCodeStyleSettings {
  protected LuaCodeStyleSettings(CodeStyleSettings container) {
    super("LuaCodeStyleSettings", container);
  }
}
