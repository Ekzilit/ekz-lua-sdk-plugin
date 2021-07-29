package ekz.psi.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import ekz.codeinsight.navigation.LuaClassHelper;
import ekz.psi.LuaBody;
import ekz.psi.LuaClass;
import ekz.psi.LuaClassMethodDefinition;
import ekz.psi.LuaClassMethodName;
import ekz.psi.LuaClassVarDefinition;
import ekz.psi.LuaClassVarName;
import ekz.psi.LuaClassVarType;
import ekz.psi.LuaForStatement;
import ekz.psi.LuaFuncAssignment;
import ekz.psi.LuaFuncAttributes;
import ekz.psi.LuaFuncDef;
import ekz.psi.LuaFuncDefName;
import ekz.psi.LuaFuncLocalDef;
import ekz.psi.LuaFuncParams;
import ekz.psi.LuaIdName;
import ekz.psi.LuaLocalVarAssignment;
import ekz.psi.LuaNamedElement;
import ekz.psi.LuaParentName;
import ekz.psi.LuaVarAssignment;
import ekz.psi.LuaVarId;
import ekz.psi.impl.LuaPsiImplUtil;
import ekz.psi.stubs.index.LuaGVarIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class LuaIdNameReference extends PsiReferenceBase<LuaNamedElement> implements PsiPolyVariantReference {

	public LuaIdNameReference(@NotNull final LuaNamedElement element, final TextRange rangeInElement) {
		super(element, rangeInElement);
	}

	@NotNull
	@Override
	public ResolveResult[] multiResolve(final boolean b) {
		if (myElement instanceof LuaIdName) {
			if (isFirstChildOfVarId() || myElement.getParent() instanceof LuaParentName ||
					myElement.getParent() instanceof LuaFuncDefName || myElement.getParent() instanceof LuaClassVarType) {
				if (((LuaIdName) myElement).isLocal()) {
					return getLocalVarResolveResult();
				} else if (((LuaIdName) myElement).isGlobal()) {
					final var classImport = LuaPsiImplUtil.isClassName(myElement);
					if (Objects.nonNull(classImport)) {
						final var reference = classImport.getClassNameWithPath().getReference().resolve();
						if (Objects.nonNull(reference)) {
							var result = new ResolveResult[1];
							result[0] = new PsiElementResolveResult(reference);
							return result;
						} else {
							return new ResolveResult[0];
						}
					}

					//TODO check first assignment by order of file with assignment in toc file, first = initial assignment
					var key = Objects.nonNull(PsiTreeUtil.getParentOfType(myElement, LuaClass.class)) ? "CLASS_GVAR" : "GVAR";
					return LuaGVarIndex.INSTANCE.get(key, myElement.getProject(),
							GlobalSearchScope.allScope(myElement.getProject()))
							.stream()
							.filter(luaIdName -> myElement.getText().equals(luaIdName.getText()) &&
									((luaIdName.getParent() instanceof LuaVarId &&
											luaIdName.getParent().getParent() instanceof LuaVarAssignment &&
											luaIdName.getParent().getChildren().length == 1) ||
											(luaIdName.getParent() instanceof LuaFuncDefName &&
													luaIdName.getParent().getChildren().length == 1)))
							.map(PsiElementResolveResult::new)
							.toArray(ResolveResult[]::new);
				} else if (((LuaIdName) myElement).isFunctionAttribute()) {
					return getFuncAttributeResolveResult();
				}
			} else if (isCurrentClassElement()) {
				//TODO redo on this
				var luaClass = (LuaClass) PsiTreeUtil.findFirstParent(myElement, LuaClass.class::isInstance);
				if (Objects.nonNull(luaClass)) {
					final var elements = luaClass.getClassBody().getClassElementDefinitionList();
					var result = elements.stream()
							.filter(classElementDefinition ->
									(classElementDefinition.getFirstChild() instanceof LuaClassVarDefinition &&
											myElement.getText()
													.equals(((LuaClassVarDefinition) classElementDefinition.getFirstChild()).getClassVarName())) ||
											(classElementDefinition.getFirstChild() instanceof LuaClassMethodDefinition &&
													myElement.getText()
															.equals(((LuaClassMethodDefinition) classElementDefinition.getFirstChild())
																	.getClassMethodName())))
							.map(PsiElementResolveResult::new)
							.toArray(ResolveResult[]::new);
					if (result.length > 0) {
						return result;
					} else {
						return getParentElementReferenceResult(luaClass);
					}
				}
			} else if (isParentClassElement()) {
				//TODO redo on this and multiparents
				var classDefinition = (LuaClass) PsiTreeUtil.findFirstParent(myElement, LuaClass.class::isInstance);
				return getParentElementReferenceResult(classDefinition);
			}
		}
		return new ResolveResult[0];
	}

	private ResolveResult[] getParentElementReferenceResult(LuaClass luaClass) {
		var parentHeader = LuaClassHelper.getParentClass(luaClass);
		if (Objects.nonNull(parentHeader)) {
			var parentClassDefinition = parentHeader.getParent();
			while (Objects.nonNull(parentClassDefinition)) {
				final var parentElements = ((LuaClass) parentClassDefinition).getClassBody().getClassElementDefinitionList();
				var result = parentElements.stream()
						.filter(classElementDefinition ->
								(classElementDefinition.getFirstChild() instanceof LuaClassVarDefinition && myElement.getText()
										.equals(((LuaClassVarDefinition) classElementDefinition.getFirstChild()).getClassVarName())) ||
										(classElementDefinition.getFirstChild() instanceof LuaClassMethodDefinition &&
												myElement.getText()
														.equals(((LuaClassMethodDefinition) classElementDefinition.getFirstChild())
																.getClassMethodName())))
						.map(PsiElementResolveResult::new)
						.toArray(ResolveResult[]::new);
				if (result.length > 0) {
					return result;
				} else {
					parentHeader = LuaClassHelper.getParentClass(parentClassDefinition);
					if (Objects.nonNull(parentHeader)) {
						parentClassDefinition = parentHeader.getParent();
					} else {
						//Object
						break;
					}
				}
			}
		}
		return new ResolveResult[0];
	}

	private boolean isFirstChildOfVarId() {
		return myElement.getParent() instanceof LuaVarId && myElement.getParent().getFirstChild().equals(myElement);
	}

	private boolean isCurrentClassElement() {
		if (myElement.getParent() instanceof LuaVarId) {
			final var children = myElement.getParent().getChildren();
			if (children.length >= 2) {
				return children[0].getText().equals("self") && children[1].isEquivalentTo(myElement);
			}
		}
		return false;
	}

	private boolean isParentClassElement() {
		if (myElement.getParent() instanceof LuaVarId) {
			final var children = myElement.getParent().getChildren();
			if (children.length >= 3) {
				return children[0].getText().equals("self") && children[1].getText().equals("parent") &&
						children[2] instanceof LuaFuncParams && children[3].isEquivalentTo(myElement);
			}
		}
		return false;
	}

	private ResolveResult[] getFuncAttributeResolveResult() {
		ResolveResult[] result = null;
		var parent = myElement.getParent();
		while (!(parent instanceof PsiFile)) {
			if (parent instanceof LuaFuncAssignment) {
				result = searchInFuncAttributes(((LuaFuncAssignment) parent).getFuncAttributes());
			} else if (parent instanceof LuaFuncDef) {
				result = searchInFuncAttributes(((LuaFuncDef) parent).getFuncAttributes());
			} else if (parent instanceof LuaFuncLocalDef) {
				result = searchInFuncAttributes(((LuaFuncLocalDef) parent).getFuncAttributes());
			}
			if (Objects.isNull(result) || result.length == 0) {
				parent = parent.getParent();
			} else {
				return result;
			}
		}
		return Objects.isNull(result) ? new ResolveResult[0] : result;
	}

	private ResolveResult[] getLocalVarResolveResult() {
		ResolveResult[] result = null;
		var parent = myElement.getParent();
		while (!(parent instanceof PsiFile)) {
			if (parent instanceof LuaBody) {
				result = searchLocalAssignmentsInChildren(parent);
			} else if (parent instanceof LuaForStatement) {
				result = searchLocalAssignmentsInChildren(parent);
				if (Objects.isNull(result) || result.length == 0) {
					if (Objects.nonNull(((LuaForStatement) parent).getForCondition())) {
						if (myElement.getFirstChild()
								.getText()
								.equals(((LuaForStatement) parent).getForCondition().getIdName().getText())) {
							result = new ResolveResult[]{
									new PsiElementResolveResult(((LuaForStatement) parent).getForCondition().getIdName())
							};
						}
					} else if (Objects.nonNull(((LuaForStatement) parent).getForPCondition())) {
						result = ((LuaForStatement) parent).getForPCondition()
								.getIdNameList()
								.stream()
								.filter(luaIdName -> myElement.getFirstChild().getText().equals(luaIdName.getText()))
								.map(PsiElementResolveResult::new)
								.toArray(ResolveResult[]::new);
					}
				}
			} else if (parent instanceof LuaClass) {
				result = fillFromClassElementNames(result, parent);
			}
			if (Objects.isNull(result) || result.length == 0) {
				parent = parent.getParent();
			} else {
				return result;
			}
		}
		result = searchLocalAssignmentsInChildren(parent);
		return Objects.isNull(result) ? new ResolveResult[0] : result;
	}

	private ResolveResult[] fillFromClassElementNames(ResolveResult[] result, PsiElement parent) {
		var elementNamePsi = PsiTreeUtil.findChildrenOfAnyType(parent, LuaClassMethodName.class, LuaClassVarName.class)
				.stream()
				.filter(luaNamedElement -> myElement.getText().equals(luaNamedElement.getText().replaceAll("\"", "")))
				.findFirst()
				.orElse(null);
		if (Objects.nonNull(elementNamePsi)) {
			result = new ResolveResult[]{new PsiElementResolveResult(elementNamePsi)};
		} else {
			var parentsElement = findInParents(myElement, LuaPsiImplUtil.getParentsClasses((LuaClass) parent));
			if (Objects.nonNull(parentsElement)) {
				result = new ResolveResult[]{new PsiElementResolveResult(parentsElement)};
			}
		}
		return Objects.isNull(result) ? new ResolveResult[0] : result;
	}

	private LuaNamedElement findInParents(LuaNamedElement element, List<LuaClass> parentsClasses) {
		return parentsClasses.stream()
				.map(parentClass -> PsiTreeUtil.findChildrenOfAnyType(parentClass, LuaClassMethodName.class,
						LuaClassVarName.class)
						.stream()
						.filter(luaNamedElement -> element.getText().equals(luaNamedElement.getText().replaceAll("\"", "")))
						.findFirst()
						.orElseGet(() -> {
							var parentParensClasses = LuaPsiImplUtil.getParentsClasses(parentClass);
							return Objects.nonNull(parentParensClasses) ? findInParents(element, parentParensClasses) : null;
						}))
				.filter(Objects::nonNull)
				.findFirst()
				.orElse(null);
	}

	@Nullable
	private ResolveResult[] searchInFuncAttributes(LuaFuncAttributes funcAttributes) {
		if (Objects.isNull(funcAttributes)) {
			return null;
		}
		var result = funcAttributes.getIdNameList()
				.stream()
				.filter(luaIdName -> myElement.getFirstChild().getText().equals(luaIdName.getText()))
				.map(PsiElementResolveResult::new)
				.toArray(ResolveResult[]::new);
		return result.length == 0 ? null : result;
	}

	@Nullable
	private ResolveResult[] searchLocalAssignmentsInChildren(PsiElement parent) {
		final var localAssignment = PsiTreeUtil.getChildrenOfTypeAsList(parent, LuaLocalVarAssignment.class)
				.stream()
				.filter(psiElement -> psiElement.getVarIdList().stream().anyMatch(this::containsMyElement))
				.findFirst()
				.orElse(null);
		if (Objects.nonNull(localAssignment)) {
			var luaVarId = localAssignment.getVarIdList().stream().filter(this::containsMyElement).findFirst();
			if (luaVarId.isPresent()) {
				return new ResolveResult[]{new PsiElementResolveResult(luaVarId.get().getIdNameList().get(0))};
			}
		}
		final var luaFuncLocalDef = PsiTreeUtil.getChildrenOfTypeAsList(parent, LuaFuncLocalDef.class)
				.stream()
				.filter(psiElement -> psiElement.getFuncDefName()
						.getIdNameList()
						.get(0)
						.getText()
						.equals(myElement.getFirstChild().getText()))
				.findFirst()
				.orElse(null);
		if (Objects.nonNull(luaFuncLocalDef)) {
			return new ResolveResult[]{new PsiElementResolveResult(luaFuncLocalDef.getFuncDefName().getIdNameList().get(0))
			};
		}
		return null;
	}

	private boolean containsMyElement(LuaVarId luaVarId) {
		return !"_".equals(luaVarId.getText()) &&
				luaVarId.getIdNameList().get(0).getText().equals(myElement.getFirstChild().getText());
	}

	@Nullable
	@Override
	public PsiElement resolve() {
		var resolveResults = multiResolve(false);
		return resolveResults.length >= 1 ? resolveResults[0].getElement() : null;
	}

	@Override
	public PsiElement handleElementRename(@NotNull final String newElementName) throws IncorrectOperationException {
		return myElement.setName(newElementName);
	}
}
