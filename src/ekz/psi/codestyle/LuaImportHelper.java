package ekz.psi.codestyle;

import com.intellij.lang.LanguageImportStatements;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import ekz.psi.LuaClass;
import ekz.psi.LuaClassPackageDefinition;
import ekz.psi.LuaElementFactory;
import ekz.psi.LuaImportDefinition;
import ekz.psi.LuaImportList;
import ekz.psi.stubs.index.LuaClassIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LuaImportHelper {

  public void addImport(PsiFile file, Editor editor, String importName) {

    final var project = file.getProject();
    final var foundCLasses = new ArrayList<>(
        LuaClassIndex.INSTANCE.get(importName, project, GlobalSearchScope.allScope(project)));

    if (foundCLasses.size() > 1) {
      final var step = new BaseListPopupStep<String>("Multiple classes found", foundCLasses.stream()
          .map(luaClass -> (
              PsiTreeUtil.getChildOfType(luaClass.getContainingFile().getFirstChild(), LuaClassPackageDefinition.class)
                  .getClassPackage()
                  .getUnquotedText() + "" + luaClass.getClassHeader().getClassName().getUnquotedText()))
          .collect(Collectors.toList())) {
        @Override
        public PopupStep onChosen(final String selectedValue, boolean finalChoice) {
          performAddImport(file, selectedValue);
          return FINAL_CHOICE;
        }
      };
      JBPopupFactory.getInstance().createListPopup(step).showInBestPositionFor(editor);
    } else if (foundCLasses.size() == 1) {
      performAddImport(file, foundCLasses);
    }
  }

  private void performAddImport(PsiFile file, List<LuaClass> foundCLasses) {
    var importNameToCreate = foundCLasses.get(0).getClassPackageDefinition().getClassPackage().getUnquotedText() + "" +
        foundCLasses.get(0).getClassHeader().getClassName().getUnquotedText();
    performAddImport(file, importNameToCreate);
  }

  private void performAddImport(PsiFile file, String importNameToCreate) {
    WriteCommandAction.writeCommandAction(file.getProject()).run(() -> {
      var importList = PsiTreeUtil.getChildOfType(file.getFirstChild(), LuaImportList.class);
      var importToCreate = LuaElementFactory.createImportElement(file.getProject(), importNameToCreate);
      if (Objects.nonNull(importList)) {
        var importExists = isImportExist(importList, importNameToCreate);
        if (!importExists) {
          addImportToImportList(file, importList, importToCreate);
        }
      } else {
        addImportList(file, importToCreate);
      }
    });
  }

  private void addImportList(PsiFile file, LuaImportDefinition importToCreate) {
    var luaClass = file.getFirstChild();
    final var packagePsi = PsiTreeUtil.getChildOfType(luaClass, LuaClassPackageDefinition.class);
    if (Objects.nonNull(packagePsi)) {
      luaClass.addAfter(LuaElementFactory.createImportListElement(file.getProject(), List.of(importToCreate)), packagePsi);
      var importList = PsiTreeUtil.getChildOfType(luaClass, LuaImportList.class);
      if (Objects.nonNull(importList)) {
        luaClass.addBefore(LuaElementFactory.createCRLF(file.getProject()), importList);
        var optimizers = LanguageImportStatements.INSTANCE.forFile(file);
        optimizers.forEach(importOptimizer -> importOptimizer.processFile(file).run());
      } else {
        //TODO error importList was no created
      }
    } else {
      //TODO error class has to have package
    }
  }

  private void optimizeImports(PsiFile file) {
    var optimizers = LanguageImportStatements.INSTANCE.forFile(file);
    optimizers.forEach(importOptimizer -> importOptimizer.processFile(file).run());
  }

  private void addImportToImportList(PsiFile file, LuaImportList importList, LuaImportDefinition importToCreate) {
    final var firstImport = importList.getFirstChild();
    if (Objects.nonNull(firstImport)) {
      importList.addBefore(LuaElementFactory.createCRLF(file.getProject()), firstImport);
      importList.addBefore(importToCreate, firstImport);
      importList.addBefore(LuaElementFactory.createCRLF(file.getProject()), firstImport);
      optimizeImports(file);
    }
  }

  private boolean isImportExist(LuaImportList importList, String importNameToCreate) {
    return importList.getImportDefinitionList()
        .stream()
        .anyMatch(importDefinition -> importNameToCreate.equals(importDefinition.getClassNameWithPath().getUnquotedText()));
  }

  public String getFullClassPathFromImportsByName(String className, LuaImportList importList) {
    return importList.getImportDefinitionList()
        .stream()
        .filter(luaImportDefinition -> luaImportDefinition.getClassNameWithPath().getUnquotedText().endsWith(className))
        .findFirst()
        .map(luaImportDefinition -> luaImportDefinition.getClassNameWithPath().getUnquotedText())
        .orElse(null);
  }
}
