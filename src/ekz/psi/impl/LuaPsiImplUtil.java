package ekz.psi.impl;

import com.intellij.icons.AllIcons;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.PsiElementBase;
import com.intellij.psi.util.PsiTreeUtil;
import ekz.LuaIcons;
import ekz.codeinsight.navigation.LuaClassHelper;
import ekz.psi.LuaBody;
import ekz.psi.LuaClass;
import ekz.psi.LuaClassMethodDefinition;
import ekz.psi.LuaClassMethodName;
import ekz.psi.LuaClassPackage;
import ekz.psi.LuaClassVarDefinition;
import ekz.psi.LuaClassVarName;
import ekz.psi.LuaClassVarType;
import ekz.psi.LuaDoBlock;
import ekz.psi.LuaElementFactory;
import ekz.psi.LuaElseIfStatement;
import ekz.psi.LuaElseStatement;
import ekz.psi.LuaFile;
import ekz.psi.LuaForCondition;
import ekz.psi.LuaForPCondition;
import ekz.psi.LuaForStatement;
import ekz.psi.LuaFuncAssignment;
import ekz.psi.LuaFuncAttributes;
import ekz.psi.LuaFuncDef;
import ekz.psi.LuaFuncDefName;
import ekz.psi.LuaFuncLocalDef;
import ekz.psi.LuaIdName;
import ekz.psi.LuaIfStatement;
import ekz.psi.LuaImportDefinition;
import ekz.psi.LuaImportList;
import ekz.psi.LuaInterfaceName;
import ekz.psi.LuaLocalVarAssignment;
import ekz.psi.LuaNamedElement;
import ekz.psi.LuaParentName;
import ekz.psi.LuaRepeatStatement;
import ekz.psi.LuaTableElement;
import ekz.psi.LuaVarId;
import ekz.psi.LuaWhileStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class LuaPsiImplUtil {
	private LuaPsiImplUtil() {
	}

	public static boolean isGlobal(LuaNamedElement element) {
		return !(isLocal(element) || isFunctionAttribute(element) || (isPartOfVar(element) && !isVarTypeIdName(element)));
	}

	public static LuaImportDefinition isClassName(LuaNamedElement element) {
		var importList = PsiTreeUtil.getChildOfType(PsiTreeUtil.getParentOfType(element, LuaClass.class), LuaImportList.class);
		var imports = Objects.nonNull(importList) ? importList.getImportDefinitionList() : new ArrayList<LuaImportDefinition>();
		if (!imports.isEmpty()) {
			return imports.stream()
					.filter(classImport -> element.getText()
							.equals(LuaClassHelper.getClassNameFromFullPath(
									classImport.getClassNameWithPath().getUnquotedText())))
					.findFirst()
					.orElse(null);
		}
		return null;
	}

	@NotNull
	public static String getUnquotedText(PsiElement psiElement) {
		var text = psiElement.getText();
		return Objects.nonNull(text) ? text.replaceAll("\"", "") : "";
	}

	public static ItemPresentation getPresentation(PsiElementBase element) {
		return new ItemPresentation() {
			@Nullable
			@Override
			public String getPresentableText() {
				if (element instanceof LuaClass) {
					return ((LuaClass) element).getClassHeader().getText();
				}
              /*  if (element instanceof LuaTableElement) {
                    var idName = ((LuaTableElement) element).getIdName();
                    return Objects.nonNull(idName) ? idName.getText() : ((LuaTableElement) element).getName();
                }*/
				return element.getName();
			}

			@NotNull
			@Override
			public String getLocationString() {
				return ((PsiElement) element).getContainingFile().getName();
			}

			@Nullable
			@Override
			public Icon getIcon(boolean b) {
				if (element instanceof LuaClassMethodDefinition) {
					return AllIcons.Nodes.Method;
				} else if (element instanceof LuaClassVarDefinition) {
					return AllIcons.Nodes.Variable;
				} else if (element instanceof LuaClass) {
					return AllIcons.Nodes.Class;
				}
				return LuaIcons.FILE;
			}
		};
	}

	public static boolean isPartOfVar(PsiElement element) {
		return isPartOfVarElement(element);
       /* if (element.getParent() instanceof LuaFuncName) {
            var funcCall = element.getParent()
                .getParent()
                .getParent();
            if (funcCall instanceof LuaFuncCall) {
                return isPartOfVarElement(funcCall);
            }

        }*/
	}

	private static boolean isPartOfVarElement(PsiElement element) {
		return Objects.nonNull(element.getPrevSibling()) &&
				((":".equals(element.getPrevSibling().getText())) || ("".equals(element.getPrevSibling().getText())));
	}

	public static boolean isLocal(LuaNamedElement element) {
		if (element instanceof LuaIdName && Objects.isNull(element.getPrevSibling())) {
			if (isTableElement(element)) {
				return true;
			}
			if (isLocalVarIdAssignment(element)) {
				return true;
			}
			if (isFuncLocalDefName(element)) {
				return true;
			}
			var parent = element.getParent();
			if (element.getParent() instanceof LuaFuncDefName && element.getParent().getParent() instanceof LuaFuncDef) {
				parent = element.getParent().getParent().getParent();
			}
			while (!(parent instanceof PsiFile)) {
				if (bodyHasLocalIdWithElement(element, parent) || funcAssignmentHasLocalIdWithElement(element, parent) ||
						funcLocalDefHasLocalIdWithElement(element, parent) || funcDefHasLocalIdWithElement(element, parent) ||
						statementHasLocalIdWithElement(element, parent) || forStatementHasElement(element, parent) ||
						forPStatementHasElement(element, parent)) {
					return true;
				}
				if (parent instanceof LuaParentName || parent instanceof LuaInterfaceName) {
					return false;
				}
				//func attribute
				if (parent instanceof LuaFuncAssignment && isFuncAssignment(element, (LuaFuncAssignment) parent)) {
					return false;
				}
				//class element deep
				if (parent instanceof LuaClass) {
					if (PsiTreeUtil.findChildrenOfAnyType(parent, LuaClassMethodName.class, LuaClassVarName.class)
							.stream()
							.anyMatch(luaNamedElement -> element.getText()
									.equals(luaNamedElement.getText().replaceAll("\"", "")))) {
						return true;
					} else {
						var parentsClasses = getParentsClasses((LuaClass) parent);
						if (checkInParents(element, parentsClasses)) {
							return true;
						}
					}
				}
				parent = parent.getParent();
			}
			if (parent instanceof LuaFile) {
				return hasLocalIdWIthElement(element, parent);
			}
		} else if (element instanceof LuaIdName) {
			if (forStatementHasElement(element, element.getParent())) {
				return true;
			} else return forPStatementHasElement(element, element.getParent());
		}
		return false;
	}

	private static boolean checkInParents(LuaNamedElement element, List<LuaClass> parentsClasses) {
		return parentsClasses.stream().anyMatch(luaClass -> {
			if (PsiTreeUtil.findChildrenOfAnyType(luaClass, LuaClassMethodName.class, LuaClassVarName.class)
					.stream()
					.anyMatch(luaNamedElement -> element.getText().equals(luaNamedElement.getText().replaceAll("\"", "")))) {
				return true;
			}
			var parentParentsClasses = getParentsClasses(luaClass);
			return Objects.nonNull(parentParentsClasses) && checkInParents(element, parentParentsClasses);
		});
	}

	public static List<LuaClass> getParentsClasses(LuaClass luaClass) {
		var parentClass = Optional.ofNullable(luaClass.getClassHeader().getParentName())
				.map(LuaParentName::getIdName)
				.map(PsiElement::getReference)
				.map(PsiReference::resolve)
				.orElse(null);
		if (Objects.nonNull(parentClass)) {
			return Collections.singletonList((LuaClass) parentClass);
		} else {
			return Optional.ofNullable(luaClass.getClassHeader().getParentNamesList())
					.map(parentNamesList -> parentNamesList.getParentNameList()
							.stream()
							.map(parentName -> (LuaClass) Optional.ofNullable(parentName.getIdName().getReference())
									.map(PsiReference::resolve)
									.get())
							.filter(Objects::nonNull)
							.collect(Collectors.toList()))
					.orElse(Collections.emptyList());
		}
	}

	private static boolean isFuncAssignment(LuaNamedElement element, LuaFuncAssignment parent) {
		var funcAttributes = parent.getFuncAttributes();
		return Objects.nonNull(funcAttributes) && !funcAttributes.getIdNameList().isEmpty() &&
				containsElement(element, funcAttributes.getIdNameList());
	}

	private static boolean forPStatementHasElement(LuaNamedElement element, PsiElement parent) {
		return (parent instanceof LuaForPCondition && ((LuaForPCondition) parent).getIdNameList()
				.stream()
				.anyMatch(luaIdName -> element.getText().equals(luaIdName.getText()))) ||
				(parent instanceof LuaForStatement && Objects.nonNull(((LuaForStatement) parent).getForPCondition()) &&
						((LuaForStatement) parent).getForPCondition()
								.getIdNameList()
								.stream()
								.anyMatch(luaIdName -> element.getText().equals(luaIdName.getText())));
	}

	//TODO
/*    private static boolean bodyHasLocalAssignment(
        LuaNamedElement element,
        PsiElement parent) {
        return parent instanceof LuaBody && hasLocalIdWIthElement(element, parent);
    }*/

	private static boolean forStatementHasElement(LuaNamedElement element, PsiElement parent) {
		return (parent instanceof LuaForStatement && Objects.nonNull(((LuaForStatement) parent).getForCondition()) &&
				element.getText().equals(((LuaForStatement) parent).getForCondition().getIdName().getText())) ||
				(parent instanceof LuaForCondition && element.getText().equals(((LuaForCondition) parent).getIdName().getText()));
	}

	private static boolean statementHasLocalIdWithElement(LuaNamedElement element, PsiElement parent) {
		return isStatement(parent) && hasLocalIdWIthElement(element, parent);
	}

	private static boolean funcDefHasLocalIdWithElement(LuaNamedElement element, PsiElement parent) {
		return parent instanceof LuaFuncDef && hasLocalIdWIthElement(element, parent);
	}

	private static boolean funcLocalDefHasLocalIdWithElement(LuaNamedElement element, PsiElement parent) {
		return parent instanceof LuaFuncLocalDef && hasLocalIdWIthElement(element, parent);
	}

	private static boolean funcAssignmentHasLocalIdWithElement(LuaNamedElement element, PsiElement parent) {
		return parent instanceof LuaFuncAssignment && hasLocalIdWIthElement(element, parent);
	}

	private static boolean bodyHasLocalIdWithElement(LuaNamedElement element, PsiElement parent) {
		return parent instanceof LuaBody && hasLocalIdWIthElement(element, parent);
	}

	private static boolean isStatement(PsiElement parent) {
		return parent instanceof LuaDoBlock || parent instanceof LuaWhileStatement || parent instanceof LuaIfStatement ||
				parent instanceof LuaElseIfStatement || parent instanceof LuaElseStatement ||
				parent instanceof LuaRepeatStatement || parent instanceof LuaForStatement;
	}

	private static boolean isFuncLocalDefName(@NotNull LuaNamedElement element) {
		return element.getParent() instanceof LuaFuncDefName && element.getParent().getParent() instanceof LuaFuncLocalDef;
	}

	private static boolean isLocalVarIdAssignment(@NotNull LuaNamedElement element) {
		return element.getParent() instanceof LuaVarId && element.getParent().getParent() instanceof LuaLocalVarAssignment;
	}

	private static boolean isTableElement(@NotNull LuaNamedElement element) {
		return element.getParent() instanceof LuaTableElement;
	}

	private static boolean hasLocalIdWIthElement(LuaNamedElement element, PsiElement parent) {
		return isInLocalVarAssignment(element, parent) || isInFuncLocalDefinition(element, parent);
	}

	private static boolean isInLocalVarAssignment(LuaNamedElement element, PsiElement parent) {
		return PsiTreeUtil.getChildrenOfTypeAsList(parent, LuaLocalVarAssignment.class)
				.stream()
				.anyMatch(psiElement -> psiElement.getVarIdList()
						.stream()
						.anyMatch(luaVarId -> !"_".equals(luaVarId.getText()) &&
								containsElementFirst(element, luaVarId.getIdNameList())));
	}

	private static boolean isInFuncLocalDefinition(LuaNamedElement element, PsiElement parent) {
		return PsiTreeUtil.getChildrenOfTypeAsList(parent, LuaFuncLocalDef.class)
				.stream()
				.anyMatch(luaFuncLocalDef -> Objects.nonNull(luaFuncLocalDef.getFuncDefName()) &&
						containsElementFirst(element, luaFuncLocalDef.getFuncDefName().getIdNameList()));
	}

	private static boolean containsElementFirst(LuaNamedElement element, List<LuaIdName> idNameList) {
		return idNameList.get(0).getText().equals(element.getText());
	}

	public static boolean isFunctionAttribute(@NotNull LuaNamedElement element) {
		if (isLocal(element)) {
			return false;
		}
		if (element.getParent() instanceof LuaFuncAttributes) {
			return true;
		}
		if ((element instanceof LuaIdName) && (Objects.isNull(element.getPrevSibling()))) {
			var parent = element.getParent();
			while (!(parent instanceof PsiFile)) {
				//TODO refactoring simplify
				if (parent instanceof LuaFuncAssignment) {
					if ("self".equals(element.getText())) {
						return true;
					}
					if (isFuncAssignment(element, (LuaFuncAssignment) parent)) return true;
				}
				if (parent instanceof LuaFuncDef) {
					if ("self".equals(element.getText())) {
						return true;
					}
					var luaFuncAttributes = ((LuaFuncDef) parent).getFuncAttributes();
					if (Objects.nonNull(luaFuncAttributes) && !luaFuncAttributes.getIdNameList().isEmpty() &&
							containsElement(element, luaFuncAttributes.getIdNameList())) {
						return true;
					}
				}
				if (parent instanceof LuaFuncLocalDef) {
					if ("self".equals(element.getText())) {
						return true;
					}
					var luaFuncAttributes = ((LuaFuncLocalDef) parent).getFuncAttributes();
					if (Objects.nonNull(luaFuncAttributes) && !luaFuncAttributes.getIdNameList().isEmpty() &&
							containsElement(element, luaFuncAttributes.getIdNameList())) {
						return true;
					}
				}
				parent = parent.getParent();
			}
		}
		return false;
	}

	private static boolean containsElement(LuaNamedElement element, @NotNull List<LuaIdName> idNames) {
		return idNames.stream().anyMatch(luaIdName -> element.getText().equals(luaIdName.getText()));
	}

	public static PsiElement setName(@NotNull LuaNamedElement element, String newName) {
		var keyNode = element.getNode();
		if (keyNode != null) {
			ASTNode node;
			//TODO change name of classPackage (children of current package) which begins with current package
			if (element instanceof LuaClassPackage) {
				var currentPackageText = element.getText();
				var index = currentPackageText.lastIndexOf('.');
				newName = index < 0 ? "\"" + newName + "\"" : currentPackageText.substring(0, index + 1) + newName + "\"";
				var property = LuaElementFactory.createStringElement(element.getProject(), newName);
				node = property.getNode();
			} else {
				var property = LuaElementFactory.createNamedElement(element.getProject(), newName);
				node = property.getNode().getFirstChildNode();
			}
			element.getNode().replaceChild(keyNode.getLastChildNode(), node);
		}
		return element;
	}

	public static PsiElement getNameIdentifier(@NotNull LuaNamedElement element) {
		var keyNode = element.getNode();
		if (keyNode != null) {
			return keyNode.getPsi();
		} else {
			return null;
		}
	}

	public static String getName(LuaNamedElement element) {
		var keyNode = element.getNode();
		if (keyNode != null) {
			// IMPORTANT: Convert embedded escaped spaces to simple spaces
			var str = keyNode.getText().replaceAll("\\\\ ", " ");
			return str.substring(str.lastIndexOf("\\") + 1);
		} else {
			return null;
		}
	}

	private static boolean isVarTypeIdName(PsiElement element) {
		return element instanceof LuaIdName && Objects.nonNull(element.getParent()) &&
				element.getParent() instanceof LuaClassVarType;
	}
}
