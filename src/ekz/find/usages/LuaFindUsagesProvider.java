package ekz.find.usages;

import com.intellij.find.impl.HelpID;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import ekz.psi.LuaBeanDefinition;
import ekz.psi.LuaBeanName;
import ekz.psi.LuaClassName;
import ekz.psi.LuaClassVarBeanType;
import ekz.psi.LuaForCondition;
import ekz.psi.LuaForPCondition;
import ekz.psi.LuaFuncAttributes;
import ekz.psi.LuaFuncDefName;
import ekz.psi.LuaIdName;
import ekz.psi.LuaVarId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class LuaFindUsagesProvider implements FindUsagesProvider {
	@Nullable
	@Override
	public WordsScanner getWordsScanner() {
		return null;
	}

	@Override
	public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
		return (psiElement instanceof LuaIdName && ((psiElement.getParent() instanceof LuaVarId &&
				psiElement.getParent().getTextOffset() == psiElement.getTextOffset()) ||
				(psiElement.getParent() instanceof LuaFuncAttributes) || (psiElement.getParent() instanceof LuaForCondition) ||
				(psiElement.getParent() instanceof LuaForPCondition) || (psiElement.getParent() instanceof LuaFuncDefName &&
				((LuaFuncDefName) psiElement.getParent()).getIdNameList().size() == 1))) ||
				(psiElement instanceof LuaBeanName && psiElement.getParent() instanceof LuaBeanDefinition) ||
				psiElement instanceof LuaClassName || (psiElement instanceof LuaClassVarBeanType);
	}

	@Nullable
	@Override
	public String getHelpId(@NotNull PsiElement psiElement) {
		if (psiElement instanceof LuaClassName) {
			return HelpID.FIND_CLASS_USAGES;
		}
		return null;
	}

	@NotNull
	@Override
	public String getType(@NotNull PsiElement element) {
		var variableType = getVariableType(element);
		return Objects.nonNull(variableType) ? variableType : element.toString();
	}

	@NotNull
	@Override
	public String getDescriptiveName(@NotNull PsiElement element) {
		var variableType = getVariableType(element);
		return Objects.nonNull(variableType) ? variableType : element.toString();
	}

	@Nullable
	private String getVariableType(@NotNull PsiElement element) {
		if (element instanceof LuaIdName) {
			if (((LuaIdName) element).isGlobal()) {
				return "global variable";
			} else if (((LuaIdName) element).isLocal()) {
				return "local variable";
			}
		}
		return null;
	}

	@NotNull
	@Override
	public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
		return "change me node text";
	}
}
