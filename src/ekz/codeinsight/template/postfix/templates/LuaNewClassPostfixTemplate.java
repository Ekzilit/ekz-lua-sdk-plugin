package ekz.codeinsight.template.postfix.templates;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LuaNewClassPostfixTemplate extends PostfixTemplate {

	public LuaNewClassPostfixTemplate(@Nullable PostfixTemplateProvider provider) {
		super("new id", "new", "self:new(\"className\")", provider);
	}

	@Override
	public boolean isApplicable(@NotNull PsiElement context, @NotNull Document copyDocument, int newOffset) {
		return context.getText().equals("new");
	}

	@Override
	public void expand(@NotNull PsiElement context, @NotNull Editor editor) {
		editor.getDocument().deleteString(context.getTextRange().getStartOffset(), context.getTextRange().getEndOffset());
		TemplateManager templateManager = TemplateManager.getInstance(context.getProject());
		Template template = templateManager.createTemplate("", "");
		template.setToReformat(true);
		template.addTextSegment("self:new(\"\")");
		templateManager.startTemplate(editor, template);
		editor.getCaretModel().moveToOffset(editor.getCaretModel().getOffset() - 2);
	}
}
