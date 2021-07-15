package ekz.codeinsight.daemon;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.util.PsiTreeUtil;
import ekz.codeinsight.navigation.LuaClassHelper;
import ekz.psi.LuaClass;
import ekz.psi.LuaClassElementDefinition;
import ekz.psi.LuaClassMethodDefinition;
import ekz.psi.LuaClassVarDefinition;
import ekz.psi.stubs.StubKeys;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static ekz.codeinsight.navigation.LuaClassHelper.getParentClass;

public class LuaRelatedItemMarkerProvider extends RelatedItemLineMarkerProvider {
  @Override
  protected void collectNavigationMarkers(@NotNull PsiElement element,
                                          @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
    if (element instanceof LuaClassElementDefinition) {
      var overridenElement = getOverridenElement((LuaClassElementDefinition) element);
      if (Objects.nonNull(overridenElement)) {
        var builder = NavigationGutterIconBuilder.create(AllIcons.Gutter.OverridingMethod)
                .setTarget(overridenElement)
                .setTooltipText("Go to " + "overriden element");
        Optional.ofNullable(PsiTreeUtil.getChildOfAnyType(element, LuaClassVarDefinition.class, LuaClassMethodDefinition.class))
                .ifPresent(psiElement -> result.add(builder.createLineMarkerInfo(psiElement)));
      }
      var overridingElement = getOverridingElements((LuaClassElementDefinition) element);
      if (CollectionUtils.isNotEmpty(overridingElement)) {
        var builder = NavigationGutterIconBuilder.create(AllIcons.Gutter.OverridenMethod)
                .setTargets(overridingElement)
                .setTooltipText("Go to " + "overriding element");
        Optional.ofNullable(PsiTreeUtil.getChildOfAnyType(element, LuaClassVarDefinition.class, LuaClassMethodDefinition.class))
                .ifPresent(psiElement -> result.add(builder.createLineMarkerInfo(psiElement)));
      }
    }
  }

  private List<LuaClassElementDefinition> getOverridingElements(@NotNull LuaClassElementDefinition element) {
    var currentClass = (LuaClass) element.getParent().getParent();
    return getDeepChildrenWithElement(element, currentClass).stream()
        .map(luaClassHeader -> getSameElement(element, (LuaClass) luaClassHeader.getParent()))
        .collect(Collectors.toList());
  }

  private List<LuaClass> getDeepChildrenWithElement(LuaClassElementDefinition element, LuaClass lClass) {
    var directChildren = getDirectClassChildren(lClass);
    var withElement = directChildren.stream()
        .filter(luaClass -> Objects.nonNull(getSameElement(element, (LuaClass) luaClass)))
        .collect(Collectors.toList());
    var withoutElement = directChildren.stream()
        .filter(luaClass -> Objects.isNull(getSameElement(element, (LuaClass) luaClass)))
        .collect(Collectors.toList());
    withoutElement.forEach(luaClass -> withElement.addAll(getDeepChildrenWithElement(element, luaClass)));
    return withElement;
  }

  @NotNull
  private Collection<LuaClass> getDirectClassChildren(@NotNull LuaClass luaClass) {
    //TODO add additional check for package
    return StubIndex.getInstance()
        .getElements(StubKeys.LUA_CLASS_PARENT, LuaClassHelper.getClassPath(luaClass.getClassHeader().getClassName()),
            luaClass.getProject(), GlobalSearchScope.allScope(luaClass.getProject()), LuaClass.class);
  }

  @Nullable
  private LuaClassElementDefinition getOverridenElement(@NotNull LuaClassElementDefinition element) {
    var luaClass = element.getParent().getParent();
    var parentLuaClass = LuaClassHelper.getParentClass(luaClass);
    while (Objects.nonNull(parentLuaClass)) {
      var parentElement = getSameElement(element, parentLuaClass);
      if (Objects.nonNull(parentElement)) {
        return parentElement;
      }
      parentLuaClass = getParentClass(parentLuaClass);
    }
    return null;
  }

  private LuaClassElementDefinition getSameElement(@NotNull LuaClassElementDefinition element, @NotNull LuaClass luaClass) {
    return luaClass.getClassBody()
        .getClassElementDefinitionList()
        .stream()
        .filter(classElementDefinition -> (classElementDefinition.getFirstChild() instanceof LuaClassVarDefinition &&
            element.getFirstChild() instanceof LuaClassVarDefinition &&
            ((LuaClassVarDefinition) element.getFirstChild()).getClassVarName()
                .getText()
                .equals(((LuaClassVarDefinition) classElementDefinition.getFirstChild()).getClassVarName().getText())) ||
            (classElementDefinition.getFirstChild() instanceof LuaClassMethodDefinition &&
                element.getFirstChild() instanceof LuaClassMethodDefinition &&
                ((LuaClassMethodDefinition) element.getFirstChild()).getClassMethodName()
                    .getText()
                    .equals(((LuaClassMethodDefinition) classElementDefinition.getFirstChild()).getClassMethodName().getText())))
        .findFirst()
        .orElse(null);
  }
}
