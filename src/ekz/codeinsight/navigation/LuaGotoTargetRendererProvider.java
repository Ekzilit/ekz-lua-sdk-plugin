package ekz.codeinsight.navigation;

import com.intellij.codeInsight.navigation.GotoTargetHandler;
import com.intellij.codeInsight.navigation.GotoTargetRendererProvider;
import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.psi.PsiElement;
import ekz.ide.util.LuaClassHeaderRenderer;
import ekz.psi.LuaClassHeader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LuaGotoTargetRendererProvider implements GotoTargetRendererProvider {
  @Nullable
  @Override
  public PsiElementListCellRenderer getRenderer(@NotNull final PsiElement element,
                                                @NotNull final GotoTargetHandler.GotoData gotoData) {
    if (element instanceof LuaClassHeader) {
      return new LuaClassHeaderRenderer();
/*
        } else if (element instanceof LuaTableElement) {
            return new LuaTableElementRenderer();
*/
    }
    return null;
  }
}
