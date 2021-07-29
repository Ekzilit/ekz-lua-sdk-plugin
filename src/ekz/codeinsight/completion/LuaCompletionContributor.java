package ekz.codeinsight.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import ekz.codeinsight.navigation.LuaClassHelper;
import ekz.psi.LuaBody;
import ekz.psi.LuaClass;
import ekz.psi.LuaClassElementDefinition;
import ekz.psi.LuaClassPackageDefinition;
import ekz.psi.LuaFile;
import ekz.psi.LuaForStatement;
import ekz.psi.LuaFuncAssignment;
import ekz.psi.LuaFuncDef;
import ekz.psi.LuaFuncLocalDef;
import ekz.psi.LuaImportDefinition;
import ekz.psi.LuaLocalVarAssignment;
import ekz.psi.LuaTableElement;
import ekz.psi.LuaTypes;
import ekz.psi.LuaVarId;
import ekz.psi.codestyle.LuaImportHelper;
import ekz.psi.stubs.index.LuaClassIndex;
import ekz.psi.stubs.index.LuaFullClassNameIndex;
import ekz.psi.stubs.index.LuaGVarIndex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class LuaCompletionContributor extends CompletionContributor {

	private static final String SELF = "self";
	private static final String SUPER = "super";
	private static final LookupElementBuilder METHOD = LookupElementBuilder.create(("method(\"\", function()\nend);"))
			.withPresentableText("method")
			.withTailText(" - method modifier")
			.withInsertHandler((insertionContext, lookupElement) -> {
				CodeStyleManager.getInstance(insertionContext.getProject())
						.adjustLineIndent(insertionContext.getFile(), insertionContext.getTailOffset() - 5);
				insertionContext.getEditor().getCaretModel().moveToOffset(insertionContext.getTailOffset() - 21);
			});

	private static final LookupElementBuilder VAR_STRING = LookupElementBuilder.create("String(\"\");")
			.withPresentableText("String")
			.withTailText(" - string type modifier")
			.withInsertHandler((insertionContext, lookupElement) -> {
				insertionContext.getEditor().getCaretModel().moveToOffset(insertionContext.getTailOffset() - 3);
				ApplicationManager.getApplication().getService(LuaImportHelper.class)
						.addImport(insertionContext.getFile(), insertionContext.getEditor(), "String");
			});
	private static final LookupElementBuilder VAR_NUMBER = LookupElementBuilder.create("Number(\"\");")
			.withPresentableText("Number")
			.withTailText(" - number type modifier")
			.withInsertHandler((insertionContext, lookupElement) -> {
				insertionContext.getEditor().getCaretModel().moveToOffset(insertionContext.getTailOffset() - 3);
				ApplicationManager.getApplication().getService(LuaImportHelper.class)
						.addImport(insertionContext.getFile(), insertionContext.getEditor(), "Number");
			});
	private static final LookupElementBuilder VAR_TABLE = LookupElementBuilder.create("Map(\"\");")
			.withPresentableText("Map")
			.withTailText(" - table type modifier")
			.withInsertHandler((insertionContext, lookupElement) -> {
				insertionContext.getEditor().getCaretModel().moveToOffset(insertionContext.getTailOffset() - 3);
				ApplicationManager.getApplication().getService(LuaImportHelper.class)
						.addImport(insertionContext.getFile(), insertionContext.getEditor(), "Map");
			});
	private static final LookupElementBuilder VAR_BOOLEAN = LookupElementBuilder.create("Boolean(\"\");")
			.withPresentableText("Boolean")
			.withTailText(" - boolean type modifier")
			.withInsertHandler((insertionContext, lookupElement) -> {
				insertionContext.getEditor().getCaretModel().moveToOffset(insertionContext.getTailOffset() - 3);
				ApplicationManager.getApplication().getService(LuaImportHelper.class)
						.addImport(insertionContext.getFile(), insertionContext.getEditor(), "Boolean");
			});
	private static final LookupElementBuilder VAR_FUNCTION = LookupElementBuilder.create("Function(\"\");")
			.withPresentableText("Function")
			.withTailText(" - boolean type modifier")
			.withInsertHandler((insertionContext, lookupElement) -> {
				insertionContext.getEditor().getCaretModel().moveToOffset(insertionContext.getTailOffset() - 3);
				ApplicationManager.getApplication().getService(LuaImportHelper.class)
						.addImport(insertionContext.getFile(), insertionContext.getEditor(), "Function");
			});
	private static final List<LookupElementBuilder> VARS = Arrays.asList(VAR_STRING, VAR_NUMBER, VAR_TABLE, VAR_BOOLEAN,
			VAR_FUNCTION);
	private static final String STATIC = "static";
	private static final String FINAL = "final";
	private static final String ABSTRACT = "abstract";
	private static final String PUBLIC = "public";
	private static final String PRIVATE = "private";
	private static final String PACKAGE = "package";
	private static final String PROTECTED = "protected";
	private LuaImportHelper luaImportHelper = ApplicationManager.getApplication().getService(LuaImportHelper.class);

	public LuaCompletionContributor() {
		//class package
		addClassPackageCompletion();
		//class
		addClassCompletion();
		//import
		addImportCompletion();
		//local var
		addLocalVarCompletion();
		//self methods
		addSelfMethodsCompletion();
		//this methods
		addThisElementsCompletion();
		//class elements
		addClassElementsCompletion();
		//self elements
		addSelfElementCompletion();
		//parent methods
		addSuperMethodsCompletion();
		//parent elements
		addSuperElementsCompletion();
		//public, private, package, protected
		addAccessModifierCompletion();
		//final, abstract, static, var, method, class, enum, interface
		//TODO add missing cases
		addAfterAccessModifierCompletion();
		//after static modifier
		addAfterStaticModifierCompletion();
		//after final or abstract modifier
//    addAfterFinalOrAbstractModifierCompletion();
		addVarTypeCompletion();
		addFullClassNameCompletion();
	}

	private void addFullClassNameCompletion() {
		extend(CompletionType.BASIC, psiElement(LuaTypes.STRING).withParent(psiElement(LuaTypes.CLASS_NAME_WITH_PATH)),
				new CompletionProvider<CompletionParameters>() {
					@Override
					protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
												  @NotNull CompletionResultSet result) {
						var project = parameters.getOriginalFile().getProject();
						result.addAllElements(LuaFullClassNameIndex.INSTANCE.getAllKeys(project)
								.stream()
								.map(className -> LookupElementBuilder.create("\"" + className)
										.withInsertHandler((insertionContext, lookupElement) -> luaImportHelper.addImport(
												insertionContext.getFile(), insertionContext.getEditor(),
												lookupElement.getLookupString())))
								.collect(Collectors.toList()));
					}
				});
	}

	private void addVarTypeCompletion() {
		extend(CompletionType.BASIC, psiElement().andOr(getAfterModifierPattern(PUBLIC), getAfterModifierPattern(PRIVATE),
				getAfterModifierPattern(PACKAGE), getAfterModifierPattern(PROTECTED), getAfterModifierPattern(FINAL),
				getAfterModifierPattern(STATIC), getAfterModifierPattern(ABSTRACT)),
				new CompletionProvider<CompletionParameters>() {
					@Override
					protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
												  @NotNull CompletionResultSet result) {
						var project = parameters.getOriginalFile().getProject();
						result.addAllElements(LuaClassIndex.INSTANCE.getAllKeys(project)
								.stream()
								.map(className -> LookupElementBuilder.create(className + "(\"\");")
										.withInsertHandler((insertionContext, lookupElement) -> {
											luaImportHelper.addImport(insertionContext.getFile(), insertionContext.getEditor(),
													className);
											insertionContext.getEditor()
													.getCaretModel()
													.moveToOffset(insertionContext.getTailOffset() - 3);
										})
										.withPresentableText(className)
										.withTailText(" - class", true))
								.collect(Collectors.toList()));
						result.addElement(METHOD);
					}
				});
	}

	@NotNull
	private PsiElementPattern.Capture<PsiElement> getAfterModifierPattern(String modifier) {
		return psiElement(LuaTypes.ID).afterLeafSkipping(psiElement(LuaTypes.DOT), psiElement(LuaTypes.ID).withText(modifier));
	}

	private void addAfterStaticModifierCompletion() {
		extend(CompletionType.BASIC, getModifierAfterStaticModifierPattern(), new CompletionProvider<CompletionParameters>() {
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
										  @NotNull CompletionResultSet result) {
				result.addElement(
						LookupElementBuilder.create(FINAL).withPresentableText(FINAL).withTailText(" - final modifier"));
			}
		});
	}

	private void addAfterAccessModifierCompletion() {
		extend(CompletionType.BASIC, getModifierAfterAccessModifierPattern(), new CompletionProvider<CompletionParameters>() {
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
										  @NotNull CompletionResultSet result) {
				result.addAllElements(Arrays.asList(
						LookupElementBuilder.create(STATIC).withPresentableText(STATIC).withTailText(" - static modifier"),
						LookupElementBuilder.create(FINAL).withPresentableText(FINAL).withTailText(" - final modifier"),
						LookupElementBuilder.create(ABSTRACT)
								.withPresentableText(ABSTRACT)
								.withTailText(" - abstract modifier")));
			}
		});
	}

	private void addAccessModifierCompletion() {
		extend(CompletionType.BASIC, getAccessModifierPattern(), new CompletionProvider<CompletionParameters>() {
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
										  @NotNull CompletionResultSet result) {
				result.addAllElements(Arrays.asList(
						LookupElementBuilder.create(PUBLIC).withPresentableText(PUBLIC).withTailText(" - public access modifier"),
						LookupElementBuilder.create(PACKAGE)
								.withPresentableText(PACKAGE)
								.withTailText(" - package access modifier"), LookupElementBuilder.create(PRIVATE)
								.withPresentableText(PRIVATE)
								.withTailText(" - private access modifier"), LookupElementBuilder.create(PROTECTED)
								.withPresentableText(PROTECTED)
								.withTailText(" - protected access modifier")));
			}
		});
	}

	private void addSuperElementsCompletion() {
		extend(CompletionType.BASIC, getSuperElementsPattern(), new CompletionProvider<CompletionParameters>() {
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
										  @NotNull CompletionResultSet result) {
				var elementsNames = new HashSet<String>();
				var parentHeader = LuaClassHelper.getParentClass(parameters.getOriginalPosition());
				while (Objects.nonNull(parentHeader)) {
					elementsNames.addAll(LuaClassHelper.getElementsNames(parentHeader));
					parentHeader = LuaClassHelper.getParentClass(parentHeader);
				}
				fillResult(new ArrayList<>(elementsNames), result);
			}
		});
	}

	private void addSuperMethodsCompletion() {
		extend(CompletionType.BASIC, getSelfParentMethodPattern(), new CompletionProvider<CompletionParameters>() {
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
										  @NotNull CompletionResultSet result) {
				var elementsNames = new HashSet<String>();
				var parentHeader = LuaClassHelper.getParentClass(parameters.getOriginalPosition());
				while (Objects.nonNull(parentHeader)) {
					elementsNames.addAll(LuaClassHelper.getMethodElementsNames(parentHeader));
					parentHeader = LuaClassHelper.getParentClass(parentHeader);
				}
				fillResult(new ArrayList<>(elementsNames), result);
			}
		});
	}

	private void addSelfElementCompletion() {
		extend(CompletionType.BASIC, getSelfElementsPattern(), new CompletionProvider<CompletionParameters>() {
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
										  @NotNull CompletionResultSet result) {
				var elementsNames = LuaClassHelper.getElementsNames(
						LuaClassHelper.getCurrentClass(parameters.getOriginalPosition()));
				var parentHeader = LuaClassHelper.getParentClass(parameters.getOriginalPosition());
				while (Objects.nonNull(parentHeader)) {
					elementsNames.addAll(LuaClassHelper.getElementsNames(parentHeader));
					parentHeader = LuaClassHelper.getParentClass(parentHeader);
				}
				fillResult(new ArrayList<>(elementsNames), result);
			}
		});
	}

	private void addThisElementsCompletion() {
		extend(CompletionType.BASIC, getThisElementsPattern(), new CompletionProvider<CompletionParameters>() {
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
										  @NotNull CompletionResultSet result) {
				fillDeepElements(result, LuaClassHelper.getCurrentClass(parameters.getOriginalPosition()));
			}
		});
	}

	private void addClassElementsCompletion() {
		extend(CompletionType.BASIC, getClassElementsPattern(), new CompletionProvider<CompletionParameters>() {
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
										  @NotNull CompletionResultSet result) {
				fillDeepElements(result, LuaClassHelper.getCurrentClass(parameters.getOriginalPosition()));
			}
		});
	}

	private void addSelfMethodsCompletion() {
		extend(CompletionType.BASIC, getSelfMethodPattern(), new CompletionProvider<CompletionParameters>() {
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
										  @NotNull CompletionResultSet result) {
				fillDeepMethodElements(result, LuaClassHelper.getCurrentClass(parameters.getOriginalPosition()));
			}
		});
	}

	private void addClassPackageCompletion() {
		extend(CompletionType.BASIC, psiElement(LuaTypes.ID).inside(psiElement(LuaFile.class))
						.withSuperParent(3, psiElement(LuaFile.class))
						.andNot(psiElement(LuaTypes.ID).withSuperParent(3,
								psiElement(LuaFile.class).withChild(psiElement(LuaClassPackageDefinition.class)))),
				new CompletionProvider<CompletionParameters>() {
					@Override
					protected void addCompletions(@NotNull CompletionParameters completionParameters,
												  @NotNull ProcessingContext processingContext,
												  @NotNull CompletionResultSet completionResultSet) {
						var filePath = completionParameters.getOriginalFile().getVirtualFile().getParent().getPath();
						filePath = filePath.replaceAll("/", "");
						var classPackage = filePath.substring(filePath.indexOf("src.") + 4);
						completionResultSet.addElement(LookupElementBuilder.create("package \"" + classPackage + "\"\n")
								.withPresentableText(PACKAGE)
								.withTailText(" - class package"));
					}
				});
	}

	private void addLocalVarCompletion() {
		extend(CompletionType.BASIC, getFirstIdNamePattern(), new CompletionProvider<CompletionParameters>() {
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
										  @NotNull CompletionResultSet result) {
				var currentElement = parameters.getOriginalFile().findElementAt(parameters.getOffset());
				if (Objects.nonNull(currentElement)) {
					var parent = currentElement.getParent();
					while (!(parent instanceof PsiFile)) {
						if (parent instanceof LuaFuncAssignment) {
							//func attributes
							final var funcAttributes = ((LuaFuncAssignment) parent).getFuncAttributes();
							if (Objects.nonNull(funcAttributes)) {
								var funcAttrNames = funcAttributes.getIdNameList()
										.stream()
										.map(PsiElement::getText)
										.collect(Collectors.toList());
								fillResult(funcAttrNames, result);
							}
						} else if (parent instanceof LuaFuncDef) {
							//func attributes
							final var funcAttributes = ((LuaFuncDef) parent).getFuncAttributes();
							if (Objects.nonNull(funcAttributes)) {
								var funcAttrNames = funcAttributes.getIdNameList()
										.stream()
										.map(PsiElement::getText)
										.collect(Collectors.toList());
								fillResult(funcAttrNames, result);
							}
						} else if (parent instanceof LuaForStatement) {
							if (Objects.nonNull(((LuaForStatement) parent).getForPCondition())) {
								fillResult(((LuaForStatement) parent).getForPCondition()
										.getIdNameList()
										.stream()
										.filter(luaIdName -> !"_".equals(luaIdName.getText()))
										.map(PsiElement::getText)
										.collect(Collectors.toList()), result);
							} else if (Objects.nonNull(((LuaForStatement) parent).getForCondition())) {
								fillResult(Collections.singletonList(
										((LuaForStatement) parent).getForCondition().getIdName().getText()), result);
							}
						} else if (parent instanceof LuaBody) {
							fillResult(getChildLocalVarNames(parent, parameters.getOffset()), result);
						}
						parent = parent.getParent();
					}
					fillResult(getChildLocalVarNames(parent, parameters.getOffset()), result);
					//global vars
					final var project = currentElement.getProject();
					if (Objects.isNull(PsiTreeUtil.getParentOfType(currentElement, LuaClass.class))) {
						fillResult(LuaGVarIndex.INSTANCE.get("GVAR", project, GlobalSearchScope.allScope(project))
								.stream()
								.filter(luaIdName -> Stream.of(PUBLIC, PRIVATE, PACKAGE, PROTECTED)
										.noneMatch(s -> s.equals(luaIdName.getText())))
								.map(PsiElement::getText)
								.collect(Collectors.toList()), result);

					}
					//classes
					LuaClassIndex.INSTANCE.getAllKeys(project)
							.forEach(className -> result.addElement(LookupElementBuilder.create(className + "()")
									.withInsertHandler((insertionContext, lookupElement) -> {
										luaImportHelper.addImport(insertionContext.getFile(), insertionContext.getEditor(),
												className);
										insertionContext.getEditor()
												.getCaretModel()
												.moveToOffset(insertionContext.getTailOffset() - 1);
									})
									.withTailText(" - class", true)));
					//var getter/setter
					result.addElement(LookupElementBuilder.create("public.getter(function() return  end)")
							.withPresentableText("public.getter")
							.withInsertHandler((insertionContext, lookupElement) -> insertionContext.getEditor()
									.getCaretModel()
									.moveToOffset(insertionContext.getTailOffset() - 5)));
					result.addElement(LookupElementBuilder.create("public.setter(function()  end)")
							.withPresentableText("public.setter")
							.withInsertHandler((insertionContext, lookupElement) -> insertionContext.getEditor()
									.getCaretModel()
									.moveToOffset(insertionContext.getTailOffset() - 5)));
				}
			}
		});
	}

	private void addImportCompletion() {
		extend(CompletionType.BASIC, psiElement(LuaTypes.ID).inside(psiElement(LuaFile.class))
				.withSuperParent(3, psiElement(LuaFile.class).withChild(psiElement(LuaClassPackageDefinition.class)))
				.withSuperParent(2, PlatformPatterns.or(
						psiElement(LuaVarId.class).afterSiblingSkipping(psiElement().whitespaceCommentEmptyOrError(),
								psiElement(LuaClassPackageDefinition.class)),
						psiElement(LuaVarId.class).afterSiblingSkipping(psiElement().whitespaceCommentEmptyOrError(),
								psiElement(LuaImportDefinition.class)))), new CompletionProvider<CompletionParameters>() {
			@Override
			protected void addCompletions(@NotNull CompletionParameters completionParameters,
										  @NotNull ProcessingContext processingContext,
										  @NotNull CompletionResultSet completionResultSet) {
				completionResultSet.addElement(LookupElementBuilder.create("import \"")
						.withPresentableText("import")
						.withTailText(" - import definition")
						.withInsertHandler((insertionContext, lookupElement) -> insertionContext.getEditor()
								.getCaretModel()
								.moveToOffset(insertionContext.getTailOffset())));
			}
		});
	}

	private void addClassCompletion() {
		extend(CompletionType.BASIC, psiElement(LuaTypes.ID).inside(psiElement(LuaFile.class))
				.withSuperParent(3, psiElement(LuaFile.class).withChild(psiElement(LuaClassPackageDefinition.class)))
				.andNot(psiElement(LuaTypes.ID).withSuperParent(3,
						psiElement(LuaFile.class).withChild(psiElement(LuaClass.class))))
				.withSuperParent(2, PlatformPatterns.or(
						psiElement(LuaVarId.class).afterSiblingSkipping(psiElement().whitespaceCommentEmptyOrError(),
								psiElement(LuaClassPackageDefinition.class)),
						psiElement(LuaVarId.class).afterSiblingSkipping(psiElement().whitespaceCommentEmptyOrError(),
								psiElement(LuaImportDefinition.class)))), new CompletionProvider<CompletionParameters>() {
			@Override
			protected void addCompletions(@NotNull CompletionParameters completionParameters,
										  @NotNull ProcessingContext processingContext,
										  @NotNull CompletionResultSet completionResultSet) {
				completionResultSet.addElement(LookupElementBuilder.create("class (\"\") {\n}")
						.withPresentableText("class")
						.withTailText(" - class definition")
						.withInsertHandler((insertionContext, lookupElement) -> insertionContext.getEditor()
								.getCaretModel()
								.moveToOffset(insertionContext.getTailOffset() - 6)));
			}
		});
	}

	private void fillDeepMethodElements(@NotNull final CompletionResultSet result, final PsiElement classHeader) {
		var elementsNames = LuaClassHelper.getMethodElementsNames((LuaClass) classHeader);
		var parentHeader = LuaClassHelper.getParentClass(classHeader);
		while (Objects.nonNull(parentHeader)) {
			elementsNames.addAll(LuaClassHelper.getMethodElementsNames(parentHeader));
			parentHeader = LuaClassHelper.getParentClass(parentHeader);
		}
		fillResult(new ArrayList<>(elementsNames), result);
	}

	private void fillDeepElements(@NotNull final CompletionResultSet result, final PsiElement luaClass) {
		var elementsNames = LuaClassHelper.getElementsNames((LuaClass) luaClass);
		var parentClass = LuaClassHelper.getParentClass((LuaClass) luaClass);
		while (Objects.nonNull(parentClass)) {
			elementsNames.addAll(LuaClassHelper.getElementsNames(parentClass));
			parentClass = LuaClassHelper.getParentClass(parentClass);
		}
		fillResult(new ArrayList<>(elementsNames), result);
	}

	@NotNull
	private PsiElementPattern.Capture<PsiElement> getAccessModifierPattern() {
		return psiElement(LuaTypes.ID).andNot(psiElement(LuaTypes.ID).afterLeaf(psiElement(LuaTypes.DOT)))
				.andNot(psiElement(LuaTypes.ID).afterLeaf(psiElement(LuaTypes.COLON)))
				.andOr(psiElement().withParent(psiElement(LuaClassElementDefinition.class)),
						psiElement().withSuperParent(3, psiElement(LuaFile.class)));
	}

	@NotNull
	private PsiElementPattern.Capture<PsiElement> getModifierAfterAccessModifierPattern() {
		return psiElement(LuaTypes.ID).and(psiElement(LuaTypes.ID).afterLeaf(psiElement(LuaTypes.DOT)))
				.andOr(getAfterModifierPattern(PUBLIC), getAfterModifierPattern(PRIVATE), getAfterModifierPattern(PACKAGE),
						getAfterModifierPattern(PROTECTED))
				.and(getIdIsClassElementPartPattern());
	}

	@NotNull
	private PsiElementPattern.Capture<PsiElement> getModifierAfterStaticModifierPattern() {
		return psiElement(LuaTypes.ID).and(psiElement(LuaTypes.ID).afterLeaf(psiElement(LuaTypes.DOT)))
				.and(psiElement(LuaTypes.ID).afterLeafSkipping(psiElement(LuaTypes.DOT),
						psiElement(LuaTypes.ID).withText(STATIC)))
				.and(getIdIsClassElementPartPattern());
	}

	@NotNull
	private PsiElementPattern.Capture<PsiElement> getModifierAfterAbstractOrFinalModifierPattern() {
		return psiElement(LuaTypes.ID).and(psiElement(LuaTypes.ID).afterLeaf(psiElement(LuaTypes.DOT)))
				.andOr(psiElement(LuaTypes.ID).afterLeafSkipping(psiElement(LuaTypes.DOT),
						psiElement(LuaTypes.ID).withText(FINAL)),
						psiElement(LuaTypes.ID).afterLeafSkipping(psiElement(LuaTypes.DOT),
								psiElement(LuaTypes.ID).withText(ABSTRACT)))
				.and(getIdIsClassElementPartPattern());
	}

	private PsiElementPattern.Capture<PsiElement> getIdIsClassElementPartPattern() {
		return psiElement(LuaTypes.ID).withSuperParent(4, psiElement(LuaTableElement.class));
	}

	@NotNull
	private PsiElementPattern.Capture<PsiElement> getSelfMethodPattern() {
		return psiElement(LuaTypes.ID).afterLeafSkipping(psiElement(LuaTypes.COLON), psiElement(LuaTypes.ID).withText(SELF));
	}

	@NotNull
	private PsiElementPattern.Capture<PsiElement> getThisElementsPattern() {
		return psiElement(LuaTypes.ID).afterLeafSkipping(psiElement(LuaTypes.DOT), psiElement(LuaTypes.ID).withText("this"));
	}

	@NotNull
	private PsiElementPattern.Capture<PsiElement> getClassElementsPattern() {
		return psiElement(LuaTypes.ID).andNot(psiElement(LuaTypes.ID).afterLeaf(psiElement(LuaTypes.DOT)))
				.and(psiElement().withParent(LuaClassElementDefinition.class));
	}

	@NotNull
	private PsiElementPattern.Capture<PsiElement> getSelfParentMethodPattern() {
		return psiElement().afterLeafSkipping(psiElement(LuaTypes.COLON), psiElement(LuaTypes.RPAREN).afterLeaf(
				psiElement(LuaTypes.LPAREN).afterLeaf(psiElement(LuaTypes.ID).withText(SUPER)
						.afterLeafSkipping(psiElement(LuaTypes.COLON), psiElement(LuaTypes.ID).withText(SELF)))));
	}

	@NotNull
	private PsiElementPattern.Capture<PsiElement> getSuperElementsPattern() {
		return psiElement().afterLeafSkipping(psiElement(LuaTypes.DOT), psiElement(LuaTypes.ID).withText(SUPER));
	}

	@NotNull
	private PsiElementPattern.Capture<PsiElement> getSelfElementsPattern() {
		return psiElement(LuaTypes.ID).afterLeafSkipping(psiElement(LuaTypes.DOT), psiElement(LuaTypes.ID).withText(SELF));
	}

	@NotNull
	private PsiElementPattern.Capture<PsiElement> getFirstIdNamePattern() {
		return psiElement().andNot(psiElement().andOr(psiElement(LuaTypes.ID).afterLeaf(psiElement(LuaTypes.COLON)),
				psiElement(LuaTypes.ID).afterLeaf(psiElement(LuaTypes.DOT))));
	}

	private List<String> getChildLocalVarNames(final PsiElement parent, final int currentPosition) {
		var localVars = Arrays.stream(parent.getChildren())
				.filter(LuaLocalVarAssignment.class::isInstance)
				.map(psiElement -> ((LuaLocalVarAssignment) psiElement).getVarIdList()
						.stream()
						.filter(luaVarId -> !"_".equals(luaVarId.getText()) && luaVarId.getTextOffset() < currentPosition)
						.map(luaVarId -> luaVarId.getIdNameList().get(0).getText())
						.collect(Collectors.toList()))
				.collect(Collectors.toList())
				.stream()
				.flatMap(List::stream)
				.collect(Collectors.toList());
		localVars.addAll(Arrays.stream(parent.getChildren())
				.filter(psiElement -> psiElement instanceof LuaFuncLocalDef &&
						Objects.nonNull(((LuaFuncLocalDef) psiElement).getFuncDefName()) &&
						((LuaFuncLocalDef) psiElement).getFuncDefName().getIdNameList().size() == 1)
				.map(psiElement -> ((LuaFuncLocalDef) psiElement).getFuncDefName()
						.getIdNameList()
						.stream()
						.filter(luaIdName -> luaIdName.getTextOffset() < currentPosition)
						.map(PsiElement::getText)
						.collect(Collectors.toList()))
				.collect(Collectors.toList())
				.stream()
				.flatMap(List::stream)
				.collect(Collectors.toList()));
		return localVars;
	}

	//TODO add correct icons
	private void fillResult(final List<String> data, final CompletionResultSet result) {
		data.forEach(s -> result.addElement(LookupElementBuilder.create(s)));
	}
}
