package ekz.psi.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import ekz.find.usages.LuaContextHelper;
import ekz.psi.LuaClassVarBeanType;
import ekz.psi.LuaClassVarDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.logging.Logger;

public class LuaBeanElementReference extends PsiReferenceBase<LuaClassVarBeanType> implements PsiPolyVariantReference {
	private Logger logger = Logger.getLogger(LuaBeanElementReference.class.getName());

	public LuaBeanElementReference(@NotNull final LuaClassVarBeanType element, final TextRange rangeInElement) {
		super(element, rangeInElement);
	}

	@NotNull
	@Override
	public ResolveResult[] multiResolve(final boolean b) {
		final var valueBeanId = ((LuaClassVarDefinition) myElement.getParent().getParent()).getValue();
		var beanId = Objects.nonNull(valueBeanId) ? valueBeanId : ((LuaClassVarDefinition) myElement.getParent()
				.getParent()).getClassVarName();

		var beans = LuaContextHelper.getBeansByBeanId(beanId);

		if (!beans.isEmpty()) {
			return beans.stream().map(PsiElementResolveResult::new).toArray(ResolveResult[]::new);
		}

/*
    var fileList = LuaContextHelper.getCurrentModuleContext(myElement);
    if (fileList.size() == 1 && Objects.nonNull(beanId)) {
      var contextFile = PsiManager.getInstance(myElement.getProject()).findFile(fileList.get(0));
      result.addAll(PsiTreeUtil.getChildrenOfTypeAsList(contextFile, LuaContextDefinition.class)
          .stream()
          .map(contextDefinition -> PsiTreeUtil.getChildrenOfTypeAsList(contextDefinition.getFuncAssignment().getBody(),
              LuaBeanDefinition.class)
              .stream()
              .filter(beanDefinition -> beanId.equals(beanDefinition.getBeanName().getText()))
              .map(PsiElementResolveResult::new)
              .collect(Collectors.toList()))
          .collect(Collectors.toList())
          .stream()
          .flatMap(List::stream)
          .collect(Collectors.toList()));
    } else if (fileList.size() > 1) {
      logger.error("Found more then 1 context");
    }
*/


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

/*
  @Override
  public PsiElement handleElementRename(@NotNull final String newElementName) throws IncorrectOperationException {
    return myElement.setName(newElementName);
  }
*/
}
