package ekz.psi.search;

import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import ekz.psi.LuaClass;
import ekz.psi.LuaClassHeader;
import ekz.psi.LuaClassName;
import ekz.psi.LuaClassPackageDefinition;
import ekz.psi.LuaImportDefinition;
import ekz.psi.stubs.index.LuaParentIndex;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import static ekz.codeinsight.navigation.LuaClassHelper.getClassNameFromFullPath;
import static ekz.codeinsight.navigation.LuaClassHelper.getClassPathFromFullPath;

public class LuaClassImplementationsSearchHelper {
  private LuaClassImplementationsSearchHelper() {

  }

  @NotNull
  public static Collection<LuaClass> getChildren(@NotNull final LuaClassName className) {
    final var project = className.getProject();
    final var children = LuaParentIndex.INSTANCE.get(className.getUnquotedText(), project, GlobalSearchScope.allScope(project));
        /*LuaClassIndex.INSTANCE.get((String) LuaClassIndex.INSTANCE.getAllKeys(project).toArray()[0],
        project, GlobalSearchScope.allScope(project))
        .stream()
        .filter(potentialChildClassHeader -> !isTheSameClass(className, potentialChildClassHeader) &&
            (isParentOfChildClassHeader(className, potentialChildClassHeader) ||
                isObjectClassParentOfChildClassHeader(className, potentialChildClassHeader)))
        .collect(Collectors.toList());*/
    if (children.size() > 0) {
      var nextLevelChildren = new ArrayList<LuaClass>();
      children.forEach(luaClass -> nextLevelChildren.addAll(getChildren(luaClass.getClassHeader().getClassName())));
      if (CollectionUtils.isNotEmpty(nextLevelChildren)) {
        children.addAll(nextLevelChildren);
      }
    }
    return children;
  }

  private static boolean isTheSameClass(@NotNull final LuaClassName className, final LuaClassHeader potentialChildClassHeader) {
    return potentialChildClassHeader.isEquivalentTo(className.getParent());
  }

  private static boolean isParentOfChildClassHeader(@NotNull final LuaClassName luaClassName,
                                                    final LuaClassHeader potentialChildClassHeader) {
    final var parentName = potentialChildClassHeader.getParentName();
    return Optional.ofNullable(parentName)
        .map(luaParentName -> luaClassName.getText().equals(parentName.getText()) &&
            (PsiTreeUtil.getChildrenOfTypeAsList(potentialChildClassHeader.getContainingFile(), LuaImportDefinition.class)
                .stream()
                .anyMatch(importDefinition -> isImportOfClass(luaClassName,
                    importDefinition.getClassNameWithPath().getUnquotedText()))))
        .orElse(false);
  }

  private static boolean isObjectClassParentOfChildClassHeader(@NotNull final LuaClassName luaClassName,
                                                               final LuaClassHeader potentialChildClassHeader) {
    return Objects.isNull(potentialChildClassHeader.getParentName()) && "Object".equals(luaClassName.getUnquotedText());
  }

  private static boolean isImportOfClass(@NotNull final LuaClassName luaClassName, final String classNameWithPath) {
    return getClassNameFromFullPath(classNameWithPath).equals(luaClassName.getUnquotedText()) &&
        getClassPathFromFullPath(classNameWithPath).equals(
            PsiTreeUtil.getChildrenOfType(luaClassName.getContainingFile(), LuaClassPackageDefinition.class)[0].getClassPackage()
                .getUnquotedText());
  }

}
