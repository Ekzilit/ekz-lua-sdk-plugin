package ekz.ide;

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import ekz.psi.LuaClass;
import ekz.psi.LuaClassElementDefinition;
import ekz.psi.LuaFile;
import org.jetbrains.annotations.NotNull;

public class LuaStructureViewModel extends StructureViewModelBase implements StructureViewModel.ElementInfoProvider {
  public LuaStructureViewModel(@NotNull PsiFile psiFile, Editor editor) {
    super(psiFile, editor, new LuaStructureViewElement(psiFile));
  }

  @Override
  public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
    return false;
  }

  @Override
  public boolean isAlwaysLeaf(StructureViewTreeElement element) {
    return element instanceof LuaFile;
  }

  @Override
  protected boolean isSuitable(PsiElement element) {
    return element instanceof LuaClassElementDefinition || element instanceof LuaClass;
  }
}
