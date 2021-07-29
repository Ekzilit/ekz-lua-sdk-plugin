package ekz.find.usages;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesHandlerFactory;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LuaFindUsagesHandlerFactory extends FindUsagesHandlerFactory {
	@Override
	public boolean canFindUsages(@NotNull PsiElement element) {
		return new LuaFindUsagesProvider().canFindUsagesFor(element);
	}

	@Nullable
	@Override
	public FindUsagesHandler createFindUsagesHandler(@NotNull PsiElement element, boolean forHighlightUsages) {
		return new LuaFindUsagesHandler(element);
	}
}
