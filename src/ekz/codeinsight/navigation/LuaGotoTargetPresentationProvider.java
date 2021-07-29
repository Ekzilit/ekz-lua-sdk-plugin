package ekz.codeinsight.navigation;

import com.intellij.codeInsight.navigation.GotoTargetPresentationProvider;
import com.intellij.icons.AllIcons;
import com.intellij.navigation.TargetPresentation;
import com.intellij.psi.PsiElement;
import ekz.psi.LuaClassHeader;
import ekz.psi.LuaFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LuaGotoTargetPresentationProvider implements GotoTargetPresentationProvider {
	@Override
	public @Nullable
	TargetPresentation getTargetPresentation(@NotNull PsiElement element, boolean differentNames) {
		if (element instanceof LuaClassHeader) {
			return TargetPresentation.Companion.builder("presentable test text")
					.icon(AllIcons.Nodes.Class)
					.containerText(((LuaFile) element.getContainingFile()).getNameWithPackage())
					.presentation();
		}
		return null;
	}
}
