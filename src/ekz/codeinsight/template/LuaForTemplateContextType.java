package ekz.codeinsight.template;

import com.intellij.codeInsight.template.TemplateActionContext;
import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiUtilCore;
import ekz.Lua;
import org.jetbrains.annotations.NotNull;

public class LuaForTemplateContextType extends TemplateContextType {
	protected LuaForTemplateContextType() {
		super("LUA_CODE", "Lua");
	}

	@Override
	public boolean isInContext(@NotNull TemplateActionContext templateActionContext) {
		if (PsiUtilCore.getLanguageAtOffset(templateActionContext.getFile(), templateActionContext.getStartOffset())
				.isKindOf(Lua.INSTANCE)) {
			PsiElement element = templateActionContext.getFile().findElementAt(templateActionContext.getStartOffset());
			if (element instanceof PsiWhiteSpace) {
				return false;
			} else {
				return element != null;
			}
		} else {
			return false;
		}
	}
}
