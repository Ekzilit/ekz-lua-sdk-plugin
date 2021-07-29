package ekz.find.usages;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesOptions;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.Processor;
import ekz.psi.LuaBeanName;
import ekz.psi.LuaClass;
import ekz.psi.LuaClassHeader;
import ekz.psi.LuaClassMethodName;
import ekz.psi.LuaClassName;
import ekz.psi.LuaClassVarName;
import ekz.psi.LuaIdName;
import ekz.psi.LuaVarId;
import ekz.psi.stubs.index.LuaGVarIndex;
import ekz.psi.stubs.index.LuaParentIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

import static ekz.codeinsight.navigation.LuaClassHelper.isClassContainsClassElement;
import static ekz.find.usages.LuaContextHelper.getCurrentModuleContext;

//TODO redo http://www.jetbrains.org/intellij/sdk/docs/reference_guide/custom_language_support/find_usages.html
public class LuaFindUsagesHandler extends FindUsagesHandler {
	protected LuaFindUsagesHandler(@NotNull PsiElement psiElement) {
		super(psiElement);
	}

	@Override
	public boolean processElementUsages(@NotNull PsiElement element, @NotNull Processor<? super UsageInfo> processor,
										@NotNull FindUsagesOptions options) {
		ApplicationManager.getApplication().runReadAction(() -> {
			if (element instanceof LuaIdName) {
				if (((LuaIdName) element).isGlobal()) {
					processGlobalIdName(element, processor);
				} else if (((LuaIdName) element).isLocal() || ((LuaIdName) element).isFunctionAttribute()) {
					super.processElementUsages(element, processor, options);
				}
			} else if (element instanceof LuaClassVarName || element instanceof LuaClassMethodName) {
				processClassElementUsagesInChildren(element, processor);
				Optional.ofNullable(PsiTreeUtil.findFirstParent(element, LuaClass.class::isInstance))
						.ifPresent(classDefinition -> processClassElementUsagesBySelfInClass(element, processor,
								(LuaClass) classDefinition));
				processClassElementUsagesByResourceDefinitions(element, processor);

			} else if (element instanceof LuaBeanName) {
				var fileList = getCurrentModuleContext(element);
				if (fileList.size() == 1) {
					var contextFile = PsiManager.getInstance(element.getProject()).findFile(fileList.get(0));
					PsiTreeUtil.findChildrenOfType(contextFile, LuaBeanName.class)
							.stream()
							.filter(beanName -> beanName.getText().equals(element.getText()) && !beanName.isEquivalentTo(element))
							.forEach(beanName -> processElement(processor, beanName));
				}
			} else if (element instanceof LuaClassName && element.getParent() instanceof LuaClassHeader) {
/*
TODO make stub for luaClassNameWithPath ????
        LuaClassIndex.INSTANCE.get((String) LuaClassIndex.INSTANCE.getAllKeys(element.getProject()).toArray()[0],
            element.getProject(), GlobalSearchScope.allScope(element.getProject())).forEach(classHeader -> {
          PsiTreeUtil.findChildrenOfType(classHeader.getContainingFile(), LuaClassNameWithPath.class)
              .stream()
              .filter(luaClassNameWithPath ->
                  getClassPathFromFullPath(luaClassNameWithPath.getUnquotedText()).equals(getClassPath((LuaClassName) element)) &&
                      getClassNameFromFullPath(luaClassNameWithPath.getUnquotedText()).equals(
                          ((LuaClassName) element).getUnquotedText()))
              .forEach(luaClassNameWithPath -> processElement(processor, luaClassNameWithPath));

          if (Objects.nonNull(classHeader.getParentName()) &&
              hasImportWithClass((LuaClassHeader) element.getParent(), classHeader) &&
              ((LuaClassName) element).getUnquotedText().equals(classHeader.getParentName().getUnquotedText())) {
            processElement(processor, classHeader.getParentName());
          }
          LuaContextHelper.getBeansByClassName((LuaClassName) element)
              .forEach(luaBeanDefinition -> processElement(processor, luaBeanDefinition));
        });
*/
			}
		});
		return true;
	}

	private void processClassElementUsagesBySelfInClass(final PsiElement element, final Processor<? super UsageInfo> processor,
														@NotNull LuaClass luaClass) {
		//TODO check neediness of this method
		PsiTreeUtil.findChildrenOfType(luaClass.getClassBody(), LuaVarId.class).forEach(varId -> {
			var idNameList = varId.getIdNameList();
			if (idNameList.size() >= 2 && "self".equals(idNameList.get(0).getText()) &&
					element.getText().equals(idNameList.get(1).getText())) {
				processElement(processor, varId);
			}
		});
	}

	private void processClassElementUsagesByResourceDefinitions(final PsiElement element,
																final Processor<? super UsageInfo> processor) {
		//TODO 1. by child with resource of element's class
		//TODO 2. by child with parent's resource of element's class

	}

	private boolean processElement(@NotNull Processor<? super UsageInfo> processor, PsiElement element) {
		return processor.process(new UsageInfo(element));
	}

	private void processGlobalIdName(@NotNull PsiElement element, @NotNull Processor<? super UsageInfo> processor) {
		var key = Objects.nonNull(PsiTreeUtil.getParentOfType(element, LuaClass.class)) ? "CLASS_GVAR" : "GVAR";
		LuaGVarIndex.INSTANCE.get(key, element.getProject(), GlobalSearchScope.allScope(element.getProject()))
				.stream()
				.filter(luaIdName -> element.getFirstChild().getText().equals(luaIdName.getText()))
				.forEach(luaIdName -> processElement(processor, luaIdName));
	}

	private void processClassElementUsagesInChildren(@NotNull PsiElement element,
													 @NotNull Processor<? super UsageInfo> processor) {
		Optional.ofNullable(((LuaClass) PsiTreeUtil.findFirstParent(element, LuaClass.class::isInstance)))
				.ifPresent(luaClass -> processClassElementUsagesInChildren(element, processor, luaClass));
	}

	private void processClassElementUsagesInChildren(@NotNull PsiElement element, @NotNull Processor<? super UsageInfo> processor,
													 @NotNull LuaClass luaClass) {
		LuaParentIndex.INSTANCE.get(luaClass.getClassHeader().getClassName().getUnquotedText(), element.getProject(),
				GlobalSearchScope.allScope(element.getProject()))
				.stream()
				.forEach(lClass -> PsiTreeUtil.findChildrenOfType(lClass, LuaVarId.class).forEach(luaVarId -> {
					final var idNameList = luaVarId.getIdNameList();
					if (idNameList.size() >= 3 && idNameList.get(0).getText().equals("self") &&
							idNameList.get(1).getText().equals("parent") &&
							idNameList.get(2).getText().equals(element.getText())) {
						processElement(processor, luaVarId);
					}
					if (!isClassContainsClassElement(element, lClass)) {
						processClassElementUsagesBySelfInClass(element, processor, (LuaClass) luaClass.getParent());
						processClassElementUsagesInChildren(element, processor, lClass);
					}
				}));
	}
}
