package ekz.codeinsight.navigation;

import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import ekz.psi.LuaClass;
import ekz.psi.LuaClassElementDefinition;
import ekz.psi.LuaClassHeader;
import ekz.psi.LuaClassMethodDefinition;
import ekz.psi.LuaClassMethodName;
import ekz.psi.LuaClassName;
import ekz.psi.LuaClassPackageDefinition;
import ekz.psi.LuaClassVarName;
import ekz.psi.LuaIdName;
import ekz.psi.LuaImportDefinition;
import ekz.psi.LuaTableElement;
import ekz.psi.impl.LuaPsiImplUtil;
import ekz.psi.stubs.index.LuaClassIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class LuaClassHelper {

	private static final String OBJECT = "Object";
	private static final String EMPTY_STRING = "";

	private LuaClassHelper() {
	}

	public static boolean isClassContainsClassElement(@NotNull PsiElement element, @NotNull LuaClass luaClass) {
		return luaClass.getClassBody()
				.getClassElementDefinitionList()
				.stream()
				.anyMatch(classElementDefinition -> element.getText()
						.equals(PsiTreeUtil.findChildOfAnyType(classElementDefinition, LuaClassMethodName.class,
								LuaClassVarName.class).getText()));
	}

	@Deprecated
	public static boolean isClassElement(@NotNull LuaIdName idName) {
		return idName.getParent() instanceof LuaTableElement && Objects.nonNull(idName.getParent().getParent().getParent()) &&
				idName.getParent().getParent().getParent() instanceof LuaClass;
	}

	public static boolean isDirectChildOfObject(@NotNull final LuaClassHeader classHeader,
												final LuaClassHeader childClassHeader) {
		return Objects.isNull(childClassHeader.getParentName()) &&
				classHeader.getClassName().getUnquotedText().equals("Object") && !childClassHeader.isEquivalentTo(classHeader);
	}

	public static boolean isChildHeaderOfNonObjectHeader(@NotNull final LuaClassHeader classHeader,
														 final LuaClassHeader childClassHeader) {
		//TODO filter additionally by path
		return Objects.nonNull(childClassHeader.getParentName()) &&
				childClassHeader.getParentName().getText().equals(classHeader.getClassName().getText()) &&
				hasImportWithClass(classHeader, childClassHeader);
	}

	public static boolean hasImportWithClass(@NotNull final LuaClassHeader classHeader, final LuaClassHeader targetClassHeader) {
		final var importDefinitions = PsiTreeUtil.getChildrenOfType(targetClassHeader.getContainingFile(),
				LuaImportDefinition.class);
		return Objects.nonNull(importDefinitions) && Arrays.stream(importDefinitions)
				.anyMatch(importDefinition -> isImportOfClass(classHeader,
						importDefinition.getClassNameWithPath().getUnquotedText()));
	}

	private static boolean isImportOfClass(@NotNull final LuaClassHeader classHeader, final String classNameWithPath) {
		return isImportWithSameClassName(classHeader, classNameWithPath) &&
				isImportWithSameClassPath(classHeader, classNameWithPath);
	}

	private static boolean isImportWithSameClassPath(@NotNull final LuaClassHeader classHeader, final String classNameWithPath) {
		return getClassPath(classHeader.getClassName()).equals(getClassPathFromFullPath(classNameWithPath));
	}

	private static boolean isImportWithSameClassName(@NotNull final LuaClassHeader classHeader, final String classNameWithPath) {
		return classHeader.getClassName().getUnquotedText().equals(getClassNameFromFullPath(classNameWithPath));
	}

	public static String getClassPath(final LuaClassName className) {
		var classPackageDefinitions = PsiTreeUtil.getChildrenOfType(className.getContainingFile(),
				LuaClassPackageDefinition.class);
		if (Objects.nonNull(classPackageDefinitions) && classPackageDefinitions.length == 1) {
			return classPackageDefinitions[0].getClassPackage().getUnquotedText();
		}
		return EMPTY_STRING;
	}

	public static Set<String> getElementsNames(final LuaClass luaClass) {
		return getElements(luaClass).stream()
				.filter(element -> Objects.nonNull(
						PsiTreeUtil.findChildOfAnyType(element, LuaClassMethodName.class, LuaClassVarName.class)))
				.map(elementDefinition -> {
					var elementName = PsiTreeUtil.findChildOfAnyType(elementDefinition, LuaClassMethodName.class,
							LuaClassVarName.class);
					var name = LuaPsiImplUtil.getUnquotedText(elementName);
					if (elementName instanceof LuaClassMethodName) {
						name = name + "()";
					}
					return name;
				})
				.collect(Collectors.toSet());
	}

	public static Set<String> getMethodElementsNames(final LuaClass luaClass) {
		return getElements(luaClass).stream()
				.filter(element -> element.getFirstChild() instanceof LuaClassMethodDefinition)
				.map(element -> {
					var name = ((LuaClassMethodDefinition) element.getFirstChild()).getClassMethodName().getUnquotedText();
					name = name + "()";
					return name;
				})
				.collect(Collectors.toSet());
	}

	public static List<LuaClassElementDefinition> getElements(final LuaClass luaClass) {
		return Objects.nonNull(luaClass) ? luaClass.getClassBody().getClassElementDefinitionList() : Collections.emptyList();
	}

	public static LuaClass getCurrentClass(final PsiElement element) {
		if (element instanceof LuaClass) {
			return (LuaClass) element;
		}
		var luaClass = PsiTreeUtil.findFirstParent(element, LuaClass.class::isInstance);
		return Objects.nonNull(luaClass) ? (LuaClass) luaClass : null;
	}

	public static LuaClass getParentClass(final PsiElement element) {
		var currentClass = getCurrentClass(element);
		return Objects.nonNull(currentClass) ? getParentClass(currentClass) : null;
	}

	public static LuaClass getParentClass(@NotNull LuaClass initialLuaClass) {
		if (initialLuaClass.getClassHeader().getClassName().getUnquotedText().equals(OBJECT)) {
			return null;
		}
		var project = initialLuaClass.getProject();
		//TODO add check getParentNameList
		if (Objects.nonNull(initialLuaClass.getClassHeader().getParentName())) {
			var potentialParents = LuaClassIndex.INSTANCE.get(initialLuaClass.getClassHeader().getParentName().getUnquotedText(),
					project, GlobalSearchScope.allScope(project));
			return potentialParents.stream().filter(luaClass -> {
				final var imports = PsiTreeUtil.findChildrenOfType(initialLuaClass, LuaImportDefinition.class);
				return imports.stream().anyMatch(importDefinition -> {
					final var classPackage = PsiTreeUtil.getChildrenOfType(luaClass, LuaClassPackageDefinition.class);
					return Objects.nonNull(classPackage) && classPackage.length == 1 && Objects.nonNull(classPackage[0]) &&
							Objects.nonNull(importDefinition.getClassNameWithPath()) &&
							getClassPathFromFullPath(importDefinition.getClassNameWithPath().getUnquotedText()).equals(
									classPackage[0].getClassPackage().getUnquotedText()) &&
							getClassNameFromFullPath(importDefinition.getClassNameWithPath().getUnquotedText()).equals(
									luaClass.getClassHeader().getClassName().getUnquotedText());
				});
			}).findFirst().orElse(null);
		} else {
			return (LuaClass) LuaClassIndex.INSTANCE.get(OBJECT, project, GlobalSearchScope.allScope(project)).toArray()[0];
		}
	}

	@NotNull
	public static String getClassPathFromFullPath(@NotNull String unquotedFullPath) {
		final var lastIndex = unquotedFullPath.lastIndexOf('.');
		if (lastIndex == -1) {
			return EMPTY_STRING;
		} else {
			return unquotedFullPath.substring(0, lastIndex);
		}
	}

	@NotNull
	public static String getClassNameFromFullPath(@NotNull String unquotedFullPath) {
		final var lastIndex = unquotedFullPath.lastIndexOf('.');
		if (lastIndex == -1) {
			return unquotedFullPath;
		} else {
			return unquotedFullPath.substring(lastIndex + 1);
		}
	}

}
