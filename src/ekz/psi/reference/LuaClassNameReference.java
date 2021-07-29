package ekz.psi.reference;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.IncorrectOperationException;
import ekz.psi.LuaClassNameWithPath;
import ekz.psi.LuaNamedElement;
import ekz.psi.stubs.index.LuaClassIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static ekz.codeinsight.navigation.LuaClassHelper.getClassNameFromFullPath;
import static ekz.codeinsight.navigation.LuaClassHelper.getClassPathFromFullPath;

public class LuaClassNameReference extends PsiReferenceBase<LuaNamedElement> implements PsiPolyVariantReference {

	public LuaClassNameReference(@NotNull final LuaNamedElement element, final TextRange rangeInElement) {
		super(element, rangeInElement);
	}

	@NotNull
	@Override
	public ResolveResult[] multiResolve(final boolean b) {
		if (myElement instanceof LuaClassNameWithPath) {
			var project = myElement.getProject();
			var className = getClassNameFromFullPath(((LuaClassNameWithPath) myElement).getUnquotedText());
			var classPath = getClassPathFromFullPath(((LuaClassNameWithPath) myElement).getUnquotedText());
			return getClassByNameAndPath(project, className, classPath);
			//handled by idName
  /*  } else if (myElement instanceof LuaParentName) {
      var project = myElement.getProject();
      var className = ((LuaParentName) myElement).getUnquotedText();
      var classPath = Optional.ofNullable(PsiTreeUtil.getChildOfType(myElement.getContainingFile(), LuaImportList.class))
          .map(importList -> importList.getImportDefinitionList()
              .stream()
              .filter(
                  psiElement -> className.equals(getClassNameFromFullPath(psiElement.getClassNameWithPath().getUnquotedText())))
              .map(psiElement -> getClassPathFromFullPath(psiElement.getClassNameWithPath().getUnquotedText()))
              .findFirst()
              .orElse(""))
          .orElse("");
      return getClassByNameAndPath(project, className, classPath);*/
		}
		return new ResolveResult[0];
	}

	@NotNull
	private ResolveResult[] getClassByNameAndPath(Project project, String className, String classPath) {
		if (StringUtil.isEmpty(className)) {
			return new ResolveResult[0];
		}
		return LuaClassIndex.INSTANCE.get(className, project, GlobalSearchScope.allScope(project))
				.stream()
				.filter(luaClass -> className.equals(luaClass.getClassHeader().getClassName().getUnquotedText()) &&
						classPath.equals(luaClass.getClassPackageDefinition().getClassPackage().getUnquotedText()))
				.map(PsiElementResolveResult::new)
				.toArray(ResolveResult[]::new);
	}

	@Nullable
	@Override
	public PsiElement resolve() {
		var resolveResults = multiResolve(false);
		return resolveResults.length >= 1 ? resolveResults[0].getElement() : null;
	}

/*  @NotNull
  @Override
  public Object[] getVariants() {
    var project = myElement.getProject();
    if (myElement instanceof LuaClassNameWithPath) {
      return LuaFullClassNameIndex.INSTANCE.getAllKeys(project).stream().map(classFullName -> "\"" + classFullName).toArray();
    } else if (myElement instanceof LuaParentName) {
      return LuaClassIndex.INSTANCE.getAllKeys(project)
          .stream()
          .map(className -> LookupElementBuilder.create("\"" + className).withPresentableText(className))
          .toArray();
    }
    return new Object[0];
  }*/

	@Override
	public PsiElement handleElementRename(@NotNull final String newElementName) throws IncorrectOperationException {
		return myElement.setName(newElementName);
	}
}
