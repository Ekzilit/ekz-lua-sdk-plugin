package ekz.codeinsight.template.postfix.templates;

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class LuaPostfixTemplateProvider implements PostfixTemplateProvider {
	private Set<PostfixTemplate> templates;

	public LuaPostfixTemplateProvider() {
		templates = ContainerUtil.newHashSet(new LuaNewClassPostfixTemplate(this));
	}

	@NotNull
	@Override
	public Set<PostfixTemplate> getTemplates() {
		return templates;
	}

	@Override
	public boolean isTerminalSymbol(char c) {
		return c == '.';
	}

	@Override
	public void preExpand(@NotNull PsiFile psiFile, @NotNull Editor editor) {

	}

	@Override
	public void afterExpand(@NotNull PsiFile psiFile, @NotNull Editor editor) {

	}

	@NotNull
	@Override
	public PsiFile preCheck(@NotNull PsiFile psiFile, @NotNull Editor editor, int i) {
		return psiFile;
	}
}
