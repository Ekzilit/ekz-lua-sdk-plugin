package ekz.codeinsight.annotation;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import ekz.codeinsight.intention.CreateImportQuickFix;
import ekz.codeinsight.navigation.LuaClassHelper;
import ekz.psi.LuaBeanIndicator;
import ekz.psi.LuaClassElementGetterIndicator;
import ekz.psi.LuaClassElementSetterIndicator;
import ekz.psi.LuaClassIndicator;
import ekz.psi.LuaClassMethodIndicator;
import ekz.psi.LuaClassPackageIndicator;
import ekz.psi.LuaClassVarIndicator;
import ekz.psi.LuaClassVarType;
import ekz.psi.LuaContextIndicator;
import ekz.psi.LuaEnumIndicator;
import ekz.psi.LuaIdName;
import ekz.psi.LuaImportDefinition;
import ekz.psi.LuaImportIndicator;
import ekz.psi.LuaInnerClassIndicator;
import ekz.psi.LuaInnerEnumIndicator;
import ekz.psi.LuaInnerInterfaceIndicator;
import ekz.psi.LuaInterfaceIndicator;
import ekz.psi.LuaNextIndicator;
import ekz.psi.LuaPairsIndicator;
import ekz.psi.LuaRefBeanIndicator;
import ekz.psi.LuaTableElement;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

import static ekz.highlighter.LuaSyntaxHighlighter.FUNC_PARAMS;
import static ekz.highlighter.LuaSyntaxHighlighter.GLOBAL_VAR;
import static ekz.highlighter.LuaSyntaxHighlighter.KEYWORD;
import static ekz.highlighter.LuaSyntaxHighlighter.LOCAL_VAR;

public class LuaAnnotator implements Annotator {

	@Override
	public void annotate(@NotNull final PsiElement psiElement, @NotNull final AnnotationHolder annotationHolder) {
		if (psiElement instanceof LuaClassIndicator || psiElement instanceof LuaInnerClassIndicator ||
				psiElement instanceof LuaInterfaceIndicator || psiElement instanceof LuaInnerInterfaceIndicator ||
				psiElement instanceof LuaEnumIndicator || psiElement instanceof LuaInnerEnumIndicator ||
				psiElement instanceof LuaClassVarIndicator || psiElement instanceof LuaClassMethodIndicator ||
				psiElement instanceof LuaClassPackageIndicator || psiElement instanceof LuaImportIndicator ||
				psiElement instanceof LuaContextIndicator || psiElement instanceof LuaPairsIndicator ||
				psiElement instanceof LuaNextIndicator || psiElement instanceof LuaRefBeanIndicator ||
				psiElement instanceof LuaBeanIndicator || psiElement instanceof LuaClassElementGetterIndicator ||
				psiElement instanceof LuaClassElementSetterIndicator || (psiElement instanceof LuaIdName &&
				("this".equals(psiElement.getText()) || "super".equals(psiElement.getText())))) {
			annotationHolder.newAnnotation(HighlightSeverity.INFORMATION, "Keyword").textAttributes(KEYWORD).create();
		} else if (psiElement instanceof LuaTableElement &&
				!(";".equals(psiElement.getLastChild().getText()) || ",".equals(psiElement.getLastChild().getText())) &&
				!isLastTableElement(psiElement)) {
			annotationHolder.newAnnotation(HighlightSeverity.ERROR, "Comma or semicolon expected to separate table elements")
					.create();
		} else if (psiElement instanceof LuaIdName) {
			if (((LuaIdName) psiElement).isGlobal()) {
				annotationHolder.newAnnotation(HighlightSeverity.INFORMATION, "Global").textAttributes(GLOBAL_VAR).create();
			} else if (((LuaIdName) psiElement).isFunctionAttribute()) {
				annotationHolder.newAnnotation(HighlightSeverity.INFORMATION, "Function attribute").textAttributes(FUNC_PARAMS).create();
			} else if (((LuaIdName) psiElement).isLocal()) {
				annotationHolder.newAnnotation(HighlightSeverity.INFORMATION, "Local").textAttributes(LOCAL_VAR).create();
			}
		} else if (psiElement instanceof LuaClassVarType && !classImportsHaveClassVarType((LuaClassVarType) psiElement)) {
			annotationHolder.newAnnotation(HighlightSeverity.ERROR, "There is no import for " + psiElement.getText() + " type")
					.withFix(new CreateImportQuickFix()).create();
		}
	}

	private boolean classImportsHaveClassVarType(@NotNull LuaClassVarType psiElement) {
		return PsiTreeUtil.findChildrenOfType(psiElement.getContainingFile(), LuaImportDefinition.class)
				.stream()
				.anyMatch(importDefinition -> Objects.nonNull(importDefinition.getClassNameWithPath()) && psiElement.getIdName()
						.getText()
						.equals(LuaClassHelper.getClassNameFromFullPath(
								importDefinition.getClassNameWithPath().getUnquotedText())));
	}

	private boolean isLastTableElement(PsiElement tableElement) {
		final var table = tableElement.getParent();
		var tableElements = Arrays.stream(table.getChildren())
				.filter(LuaTableElement.class::isInstance)
				.sorted(Comparator.comparing(PsiElement::getStartOffsetInParent))
				.collect(Collectors.toList());
		return tableElements.get(tableElements.size() - 1).getStartOffsetInParent() == tableElement.getStartOffsetInParent();
	}
}
