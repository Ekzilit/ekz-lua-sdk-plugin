package ekz.psi.codestyle;

import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.application.options.SmartIndentOptionsEditor;
import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import ekz.Lua;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LuaLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {
	@Nullable
	@Override
	public String getCodeSample(@NotNull SettingsType settingsType) {
		return "package \"hud.facades.static\"\n" + "import \"hud.facades.static.FacadeStatic\"\n" +
				"import \"core.models.ChanneledCastModel\"\n" + "import \"core.models.CastModel\"\n" +
				"import \"core.models.params.FrameParams\"\n" + "import \"core.utils.NumberUtils\"\n" +
				"public.class(\"CastFacade\", \"FacadeStatic\") {\n" + "\n" + "  public.var.Bean(\"castService\");\n" +
				"  public.var.Bean(\"positionService\", \"sPositionService\");\n" + "\n" +
				"  public.method(\"getWidget\", function(params, unitId)\n" +
				"    local widget = super().getWidgetContainer(params, unitId)\n" +
				"    widget.header = super().getWidget(FrameParams(params.name .. \"header\", widget))\n" +
				"    widget.details = itemFacade.getWidget(FrameParams(params.name .. \"_details\", widget))\n" +
				"    widget.channelModel = ChanneledCastModel()\n" + "    widget.castModel = CastModel()\n" +
				"    widget.channel = false\n" + "    widget:Hide()\n" + "    widget.header:Hide()\n" +
				"    widget.updateFrequency = 0.1\n" + "    return widget\n" + "  end);\n" + "\n" +
				"  public.method(\"updateChannel\", function(widget)\n" + "    --TODO highlight the channel under cast\n" +
				"    widget.channel = true\n" + "    castService.updateChannel(widget.channelModel, widget.unitId)\n" +
				"    if widget.channelModel.endTime then\n" +
				"      widget.header:setText(NumberUtils.formatNumberToString(round(widget.channelModel.endTime / 1000 - GetTime(), 1)))\n" +
				"      widget.details:setText(widget.channelModel.name)\n" +
				"      widget.details:SetWidth(widget.details:getTextWidth())\n" + "    end\n" +
				"    if widget.channelModel.name == nil then\n" + "      hide(widget)\n" + "    else\n" + "      show(widget)\n" +
				"    end\n" + "  end);\n" + "\n" + "  public.method(\"updateCast\", function(widget)\n" +
				"    widget.channel = false\n" + "    castService.updateCast(widget.castModel, widget.unitId)\n" +
				"    if widget.castModel.endTime then\n" +
				"      widget.header:setText(NumberUtils.formatNumberToString(round(widget.castModel.endTime / 1000 - GetTime(), 1)))\n" +
				"      widget.details:setText(widget.castModel.name)\n" +
				"      widget.details:SetWidth(widget.details:getTextWidth())\n" + "    end\n" +
				"    if widget.castModel.name == nil then\n" + "      hide(widget)\n" + "    else\n" + "      show(widget)\n" +
				"    end\n" + "  end);\n" + "\n" + "  public.method(\"resize\", function(widget)\n" +
				"    widget:SetSize(100, 22)\n" + "    widget.header:SetSize(30, 20)\n" +
				"    widget.details:SetSize(widget.details:getTextWidth(), 15)\n" + "  end);\n" + "\n" +
				"  public.method(\"place\", function(widget)\n" + "    local point = positionService.getPoint(widget.unitId)\n" +
				"    widget:SetPoint(point, widget:GetParent(), point, 0, 160)\n" +
				"    widget.header:SetPoint(point, widget, point)\n" +
				"    widget.details:SetPoint(point, widget, point, 32, 3)\n" + "  end);\n" + "\n" +
				"  public.method(\"handle\", function(widget)\n" + "    if widget.channel then\n" +
				"      updateChannel(widget)\n" + "    else\n" + "      updateCast(widget)\n" + "    end\n" + "  end);\n" + "\n" +
				"  public.method(\"handleChannel\", function(widget)\n" + "    widget.channel = true\n" +
				"    updateChannel(widget)\n" + "    show(widget)\n" + "  end);\n" + "\n" +
				"  public.method(\"handleCast\", function(widget)\n" + "    widget.channel = false\n" +
				"    updateCast(widget)\n" + "    show(widget)\n" + "  end);\n" + "\n" +
				"  public.method(\"hide\", function(widget)\n" + "    widget:Hide()\n" + "    widget.header:Hide()\n" +
				"    widget.details:Hide()\n" + "  end);\n" + "\n" + "  public.method(\"show\", function(widget)\n" +
				"    widget:Show()\n" + "    widget.header:Show()\n" + "    widget.details:Show()\n" + "  end);\n" + "\n" +
				"  public.method(\"enable\", function(widget)\n" + "    if widget.controller then\n" +
				"      widget.controller.registerUpdateEvents()\n" + "    end\n" + "  end);\n" + "\n" +
				"  public.method(\"setup\", function(widget)\n" + "    resize(widget)\n" + "    place(widget)\n" + "  end);\n" +
				"}\n";
	}

	@Override
	public void customizeSettings(@NotNull CodeStyleSettingsCustomizable consumer, @NotNull SettingsType settingsType) {
		if (settingsType == SettingsType.SPACING_SETTINGS) {
			consumer.showStandardOptions("SPACE_AROUND_ASSIGNMENT_OPERATORS");
			consumer.renameStandardOption("SPACE_AROUND_ASSIGNMENT_OPERATORS", "Separator");
		} else if (settingsType == SettingsType.BLANK_LINES_SETTINGS) {
			consumer.showStandardOptions("KEEP_BLANK_LINES_IN_CODE");
		}
	}

	@Nullable
	@Override
	public IndentOptionsEditor getIndentOptionsEditor() {
		return new SmartIndentOptionsEditor();
	}

	@NotNull
	@Override
	public Language getLanguage() {
		return Lua.INSTANCE;
	}

	@Override
	protected void customizeDefaults(@NotNull CommonCodeStyleSettings commonSettings,
									 @NotNull CommonCodeStyleSettings.IndentOptions indentOptions) {
		indentOptions.CONTINUATION_INDENT_SIZE = 4;
		indentOptions.INDENT_SIZE = 2;
		indentOptions.TAB_SIZE = 2;
	}
}
