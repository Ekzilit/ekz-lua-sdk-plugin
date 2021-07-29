package ekz.refactoring;

import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LuaRefactoringSupportProvider extends RefactoringSupportProvider {
	@Override
	public boolean isMemberInplaceRenameAvailable(@NotNull final PsiElement element, @Nullable final PsiElement context) {
	/*	if (element instanceof TocSourceFile)
		{
			return true;
		}*/
		return super.isMemberInplaceRenameAvailable(element, context);
	}
}
