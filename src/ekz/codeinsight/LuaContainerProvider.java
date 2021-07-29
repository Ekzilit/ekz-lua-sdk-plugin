package ekz.codeinsight;

import com.intellij.codeInsight.ContainerProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import ekz.psi.LuaClass;
import ekz.psi.LuaClassMethodName;
import ekz.psi.LuaClassName;
import ekz.psi.LuaClassVarName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LuaContainerProvider implements ContainerProvider {
	@Nullable
	@Override
	public PsiElement getContainer(@NotNull final PsiElement item) {
		if (item instanceof LuaClassName) {
			return item.getContainingFile();
		} else if (item instanceof LuaClassVarName || item instanceof LuaClassMethodName) {
			return PsiTreeUtil.getParentOfType(item, LuaClass.class).getClassHeader().getClassName();
		}
		return null;
	}
}
