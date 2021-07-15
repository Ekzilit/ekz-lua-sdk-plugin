package ekz.ide;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import ekz.psi.LuaClass;
import ekz.psi.LuaClassElementDefinition;
import ekz.psi.LuaFile;
import ekz.psi.impl.LuaClassElementDefinitionImpl;
import ekz.psi.impl.LuaClassImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LuaStructureViewElement implements StructureViewTreeElement, SortableTreeElement {
  private NavigatablePsiElement element;

  public LuaStructureViewElement(NavigatablePsiElement psiFile) {
    this.element = psiFile;
  }

  @Override
  public Object getValue() {
    return element;
  }

  @NotNull
  @Override
  public String getAlphaSortKey() {
    var name = element.getName();
    return name != null ? name : "";
  }

  @NotNull
  @Override
  public ItemPresentation getPresentation() {
    var presentation = element.getPresentation();
    if (Objects.isNull(presentation)) {
      presentation = new PresentationData("fix me", "fix me", null, null);
    }
    return presentation;
  }

  @NotNull
  @Override
  public TreeElement[] getChildren() {
    if (element instanceof LuaFile) {
      var luaClasses = PsiTreeUtil.getChildrenOfType(element, LuaClass.class);
      if (Objects.isNull(luaClasses)) {
        return EMPTY_ARRAY;
      }
      List<TreeElement> treeElements = new ArrayList<>(luaClasses.length);
      for (var luaClass : luaClasses) {
        treeElements.add(new LuaStructureViewElement((LuaClassImpl) luaClass));
      }
      return treeElements.toArray(new TreeElement[0]);
    } else if (element instanceof LuaClass) {
      var classElements = PsiTreeUtil.findChildrenOfType(element, LuaClassElementDefinition.class);
      List<TreeElement> treeElements = new ArrayList<>(classElements.size());
      for (var classElement : classElements) {
        if (Objects.nonNull(classElement.getParent().getParent()) && classElement.getParent().getParent() instanceof LuaClass) {
          treeElements.add(new LuaStructureViewElement((LuaClassElementDefinitionImpl) classElement));
        }
      }
      return treeElements.toArray(new TreeElement[0]);
    } else {
      return EMPTY_ARRAY;
    }
  }

  @Override
  public void navigate(boolean requestFocus) {
    /*    Navigatable descriptor = PsiNavigationSupport.getInstance()
            .createNavigatable(element.getProject(),
                element.getContainingFile()
                    .getVirtualFile(),
                element.getTextOffset());
        descriptor.navigate(true);*/
    element.navigate(requestFocus);
  }

  @Override
  public boolean canNavigate() {
    return element.canNavigate();
  }

  @Override
  public boolean canNavigateToSource() {
    return element.canNavigateToSource();
  }

}
