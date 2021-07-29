package ekz.psi.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.util.IncorrectOperationException;
import ekz.psi.LuaTableElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LuaClassElementReference extends PsiReferenceBase<LuaTableElement> implements PsiPolyVariantReference {

	public LuaClassElementReference(@NotNull final LuaTableElement element, final TextRange rangeInElement) {
		super(element, rangeInElement);
	}

	@NotNull
	@Override
	public ResolveResult[] multiResolve(final boolean b) {
		return new ResolveResult[0];
	}

	@Nullable
	@Override
	public PsiElement resolve() {
		ResolveResult[] resolveResults = multiResolve(false);
		return resolveResults.length >= 1 ? resolveResults[0].getElement() : null;
	}

	@NotNull
	@Override
	public Object[] getVariants() {
		return new Object[0];
	}

	@Override
	public PsiElement handleElementRename(@NotNull final String newElementName) throws IncorrectOperationException {
		return myElement;// myElement.setName(newElementName);
	}
}
