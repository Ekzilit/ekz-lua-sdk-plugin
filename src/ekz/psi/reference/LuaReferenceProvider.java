package ekz.psi.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import ekz.psi.LuaBeanDefinition;
import ekz.psi.LuaBeanName;
import ekz.psi.LuaClassNameWithPath;
import ekz.psi.LuaClassPackage;
import ekz.psi.LuaClassVarBeanType;
import ekz.psi.LuaFuncDefName;
import ekz.psi.LuaIdName;
import ekz.psi.LuaNamedElement;
import ekz.psi.LuaParentName;
import org.jetbrains.annotations.NotNull;

public class LuaReferenceProvider extends PsiReferenceProvider {

	private PsiReference getReference(LuaNamedElement psiElement) {
		if (psiElement instanceof LuaParentName || psiElement instanceof LuaClassNameWithPath) {
			return new LuaClassNameReference(psiElement, new TextRange(0, psiElement.getTextLength()));
		} else if (psiElement instanceof LuaBeanName && !(psiElement.getParent() instanceof LuaBeanDefinition)) {
			return new LuaBeanNameReference((LuaBeanName) psiElement, new TextRange(0, psiElement.getTextLength()));
		} else if (psiElement instanceof LuaFuncDefName) {
			return new LuaIdNameReference(psiElement,
					((LuaFuncDefName) psiElement).getIdNameList().get(0).getTextRangeInParent());
		} else if (psiElement instanceof LuaIdName) {
			return new LuaIdNameReference(psiElement, new TextRange(0, psiElement.getTextLength()));
		} else if (psiElement instanceof LuaClassVarBeanType) {
			return new LuaBeanElementReference((LuaClassVarBeanType) psiElement, new TextRange(0, psiElement.getTextLength()));
		} else if (psiElement instanceof LuaClassPackage) {
			return new LuaClassPackageReference((LuaClassPackage) psiElement, new TextRange(0, psiElement.getTextLength()));
		}
		return null;
	}

	@NotNull
	@Override
	public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
		return new PsiReference[]{getReference((LuaNamedElement) element)};
	}
}
