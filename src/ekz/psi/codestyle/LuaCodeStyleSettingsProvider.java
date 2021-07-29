package ekz.psi.codestyle;

import com.intellij.application.options.CodeStyleAbstractConfigurable;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.psi.codeStyle.CodeStyleConfigurable;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import ekz.Lua;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LuaCodeStyleSettingsProvider extends CodeStyleSettingsProvider {
	@Nullable
	@Override
	public CustomCodeStyleSettings createCustomSettings(CodeStyleSettings settings) {
		return new LuaCodeStyleSettings(settings);
	}

	@Nullable
	@Override
	public String getConfigurableDisplayName() {
		return "Lua";
	}

	@NotNull
	public CodeStyleConfigurable createConfigurable(@NotNull CodeStyleSettings settings,
													@NotNull CodeStyleSettings modelSettings) {
		return new CodeStyleAbstractConfigurable(settings, modelSettings, this.getConfigurableDisplayName()) {
			@Override
			protected CodeStyleAbstractPanel createPanel(CodeStyleSettings settings) {
				return new LuaCodeStyleMainPanel(getCurrentSettings(), settings);
			}
		};
	}

	private static class LuaCodeStyleMainPanel extends TabbedLanguageCodeStylePanel {
		public LuaCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
			super(Lua.INSTANCE, currentSettings, settings);
		}
	}
}
