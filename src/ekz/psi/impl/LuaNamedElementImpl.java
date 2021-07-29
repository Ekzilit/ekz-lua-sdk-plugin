package ekz.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import ekz.psi.LuaNamedElement;
import org.jetbrains.annotations.NotNull;

public abstract class LuaNamedElementImpl extends ASTWrapperPsiElement implements LuaNamedElement, NavigationItem {
	public LuaNamedElementImpl(@NotNull final ASTNode node) {
		super(node);
	}

	@Override
	public PsiReference getReference() {
		var psiReferences = getReferences();
		return psiReferences.length > 0 ? psiReferences[0] : null;
	}

	@NotNull
	@Override
	public PsiReference[] getReferences() {
		return ReferenceProvidersRegistry.getReferencesFromProviders(this);
	}

}
