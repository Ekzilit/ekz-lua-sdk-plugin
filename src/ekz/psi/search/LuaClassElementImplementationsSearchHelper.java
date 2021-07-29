package ekz.psi.search;

import com.intellij.psi.util.PsiTreeUtil;
import ekz.psi.LuaClass;
import ekz.psi.LuaClassElementDefinition;
import ekz.psi.LuaClassMethodName;
import ekz.psi.LuaClassVarName;

public class LuaClassElementImplementationsSearchHelper {
	private LuaClassElementImplementationsSearchHelper() {
	}

	public static LuaClassElementDefinition getElementImplementation(final LuaClassElementDefinition element,
																	 final LuaClass luaClass) {
		final var elementList = luaClass.getClassBody().getClassElementDefinitionList();
		return elementList.stream().filter(childElement -> isTheSameElement(element, childElement)).findFirst().orElse(null);
	}

	private static boolean isTheSameElement(final LuaClassElementDefinition element,
											final LuaClassElementDefinition childElement) {
		return PsiTreeUtil.findChildOfAnyType(element, LuaClassVarName.class, LuaClassMethodName.class)
				.getText()
				.equals(PsiTreeUtil.findChildOfAnyType(childElement, LuaClassVarName.class, LuaClassMethodName.class).getText());
	}
}
