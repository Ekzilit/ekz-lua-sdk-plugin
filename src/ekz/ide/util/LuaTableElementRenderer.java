package ekz.ide.util;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.psi.PsiElement;
import ekz.psi.LuaFile;
import ekz.psi.LuaTableElement;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class LuaTableElementRenderer extends PsiElementListCellRenderer<LuaTableElement> {
  @Override
  public String getElementText(final LuaTableElement element) {
    return "";//Objects.nonNull(element.getIdName()) ? element.getIdName().getName() : element.getName();
  }

  @Nullable
  @Override
  protected String getContainerText(final LuaTableElement element, final String name) {
    return ((LuaFile) element.getContainingFile()).getNameWithPackage();
  }

  @Override
  protected int getIconFlags() {
    return 0;
  }

  @Override
  protected Icon getIcon(final PsiElement element) {
    return AllIcons.Nodes.Class;
  }

}
