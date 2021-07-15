package ekz.find.usages;

import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import ekz.LuaFileType;
import ekz.psi.LuaBeanDefinition;
import ekz.psi.LuaBeanName;
import ekz.psi.LuaClassName;
import ekz.psi.LuaClassNameWithPath;
import ekz.psi.LuaContextParentName;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ekz.codeinsight.navigation.LuaClassHelper.getClassNameFromFullPath;
import static ekz.codeinsight.navigation.LuaClassHelper.getClassPath;
import static ekz.codeinsight.navigation.LuaClassHelper.getClassPathFromFullPath;

public class LuaContextHelper {

  private LuaContextHelper() {
  }

  public static List<VirtualFile> getCurrentModuleContext(final PsiElement psiElement) {
    return FileTypeIndex.getFiles(LuaFileType.INSTANCE,
        GlobalSearchScope.moduleScope(ModuleUtil.findModuleForPsiElement(psiElement)))
        .stream()
        .filter(virtualFile -> virtualFile.getPresentableName()
            //removing .lua
            .substring(0, virtualFile.getPresentableName().length() - 4).endsWith("Context"))
        .collect(Collectors.toList());
  }

  public static List<LuaBeanDefinition> getBeansByClassName(final LuaClassName className) {
    var fileList = getCurrentModuleContext(className);
    if (fileList.size() == 1) {
      var contextFile = PsiManager.getInstance(className.getProject()).findFile(fileList.get(0));
      return PsiTreeUtil.findChildrenOfType(contextFile, LuaClassNameWithPath.class)
          .stream()
          .filter(luaClassNameWithPath ->
              getClassNameFromFullPath(luaClassNameWithPath.getUnquotedText()).equals(className.getUnquotedText()) &&
                  getClassPath(className).equals(getClassPathFromFullPath(luaClassNameWithPath.getUnquotedText())))
          .map(luaClassNameWithPath -> (LuaBeanDefinition) luaClassNameWithPath.getParent())
          .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  public static VirtualFile getContextByName(final LuaContextParentName contextName) {
    return FileTypeIndex.getFiles(LuaFileType.INSTANCE, GlobalSearchScope.projectScope(contextName.getProject()))
        .stream()
        .filter(virtualFile -> {
          var contextNameUnquoted = contextName.getUnquotedText();
          var contextNameFormatted =
              contextNameUnquoted.substring(0, 1).toUpperCase() + contextNameUnquoted.substring(1);
          return virtualFile.getPresentableName().startsWith(contextNameFormatted + "Context");
        })
        .findFirst()
        .orElse(null);
  }

  public static List<LuaBeanDefinition> getBeansByBeanId(final PsiElement beanIdName) {
    var fileList = getCurrentModuleContext(beanIdName);
    if (fileList.size() == 1) {
      var contextFile = PsiManager.getInstance(beanIdName.getProject()).findFile(fileList.get(0));
      var result = PsiTreeUtil.findChildrenOfType(contextFile, LuaBeanName.class)
          .stream()
          .filter(beanName -> beanIdName.getText().equals(beanName.getText()))
          .map(luaBeanName -> (LuaBeanDefinition) luaBeanName.getParent())
          .collect(Collectors.toList());
      if (result.isEmpty()) {
        var contextParentName = PsiTreeUtil.findChildrenOfType(contextFile, LuaContextParentName.class).toArray()[0];
        var file = getContextByName((LuaContextParentName) contextParentName);
        while (Objects.nonNull(file)) {
          var contextParentFile = PsiManager.getInstance(beanIdName.getProject()).findFile(file);
          result.addAll(PsiTreeUtil.findChildrenOfType(contextParentFile, LuaBeanName.class)
              .stream()
              .filter(beanName -> beanIdName.getText().equals(beanName.getText()))
              .map(luaBeanName -> (LuaBeanDefinition) luaBeanName.getParent())
              .collect(Collectors.toList()));

          if (result.isEmpty()) {
            final var contextParentNames = PsiTreeUtil.findChildrenOfType(contextParentFile, LuaContextParentName.class)
                .toArray();
            if (contextParentNames.length > 0) {
              file = getContextByName((LuaContextParentName) contextParentNames[0]);
            } else {
              break;
            }
          } else {
            return result;
          }
        }
      } else {
        return result;
      }
    }
    return Collections.emptyList();
  }
}
