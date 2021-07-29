package ekz.psi.reference;

import com.intellij.openapi.roots.PackageIndex;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.psi.impl.file.PsiPackageImpl;
import com.intellij.util.IncorrectOperationException;
import ekz.psi.LuaClassPackage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LuaClassPackageReference extends PsiReferenceBase<LuaClassPackage> implements PsiPolyVariantReference {

	public LuaClassPackageReference(@NotNull final LuaClassPackage element, final TextRange rangeInElement) {
		super(element, rangeInElement);
	}

	@NotNull
	@Override
	public ResolveResult[] multiResolve(final boolean b) {
		var myManager = PsiManagerEx.getInstanceEx(myElement.getProject());
		var myPackageIndex = PackageIndex.getInstance(myManager.getProject());
		var dirs = myPackageIndex.getDirsByPackageName(myElement.getUnquotedText(), true);
		if (!dirs.findAll().isEmpty()) {
			return new ResolveResult[]{new PsiElementResolveResult(new PsiPackageImpl(myManager, myElement.getUnquotedText()))
			};
		}
		return new ResolveResult[0];
	}

	@Nullable
	@Override
	public PsiElement resolve() {
		var resolveResults = multiResolve(false);
		return resolveResults.length >= 1 ? resolveResults[0].getElement() : null;
	}

	@NotNull
	@Override
	public Object[] getVariants() {
		return new Object[0];
	}

	@Override
	public PsiElement handleElementRename(@NotNull final String newElementName) throws IncorrectOperationException {
		return myElement.setName(newElementName);
	}
}
