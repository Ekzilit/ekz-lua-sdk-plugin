package ekz.psi.reference;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import ekz.find.usages.LuaContextHelper;
import ekz.psi.LuaBeanDefinition;
import ekz.psi.LuaBeanName;
import ekz.psi.LuaContextDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class LuaBeanNameReference extends PsiReferenceBase<LuaBeanName> implements PsiPolyVariantReference {
	private Logger logger = Logger.getInstance(LuaBeanNameReference.class);

	public LuaBeanNameReference(@NotNull final LuaBeanName element, final TextRange rangeInElement) {
		super(element, rangeInElement);
	}

	@NotNull
	@Override
	public ResolveResult[] multiResolve(final boolean b) {
		var fileList = LuaContextHelper.getCurrentModuleContext(myElement);
		if (fileList.size() == 1) {
			var contextFile = PsiManager.getInstance(myElement.getProject()).findFile(fileList.get(0));
			return PsiTreeUtil.getChildrenOfTypeAsList(contextFile, LuaContextDefinition.class)
					.stream()
					.map(contextDefinition -> PsiTreeUtil.getChildrenOfTypeAsList(contextDefinition.getFuncAssignment().getBody(),
							LuaBeanDefinition.class)
							.stream()
							.filter(beanDefinition -> myElement.getText().equals(beanDefinition.getBeanName().getText()))
							.map(PsiElementResolveResult::new)
							.collect(Collectors.toList()))
					.collect(Collectors.toList())
					.stream()
					.flatMap(List::stream)
					.toArray(ResolveResult[]::new);
		} else if (fileList.size() > 1) {
			logger.error("Found more then 1 context");
		}
		return new ResolveResult[0];
	}

	@Nullable
	@Override
	public PsiElement resolve() {
		var resolveResults = multiResolve(false);
		return resolveResults.length >= 1 ? resolveResults[0].getElement() : null;
	}

	@NotNull
	@Override
	public Object[] getVariants() {
		return new Object[0];
	}

	@Override
	public PsiElement handleElementRename(@NotNull final String newElementName) throws IncorrectOperationException {
		return myElement.setName(newElementName);
	}
}
