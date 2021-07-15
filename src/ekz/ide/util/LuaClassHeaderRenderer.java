package ekz.ide.util;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.psi.PsiElement;
import ekz.psi.LuaClassHeader;
import ekz.psi.LuaFile;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class LuaClassHeaderRenderer extends PsiElementListCellRenderer<LuaClassHeader> {
  @Override
  public String getElementText(final LuaClassHeader element) {
    return element.getName();
  }

  @Nullable
  @Override
  protected String getContainerText(final LuaClassHeader element, final String name) {
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
