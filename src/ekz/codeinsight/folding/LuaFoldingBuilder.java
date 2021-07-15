package ekz.codeinsight.folding;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import ekz.psi.LuaBody;
import ekz.psi.LuaFuncAssignment;
import ekz.psi.LuaImportList;
import ekz.psi.LuaTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LuaFoldingBuilder extends FoldingBuilderEx {
  @NotNull
  @Override
  public FoldingDescriptor[] buildFoldRegions(@NotNull final PsiElement root, @NotNull final Document document, final boolean b) {
    //FoldingGroup foldingGroup = FoldingGroup.newGroup("sourceFiles");
    List<FoldingDescriptor> foldingDescriptors = new ArrayList<>();
    foldingDescriptors.addAll(getFunctionAssignmentFoldingDescriptors(root));
    foldingDescriptors.addAll(getTableFoldingDescriptors(root));
    foldingDescriptors.addAll(getBodyFoldingDescriptors(root));
    final var importsFoldingDescriptors = getImportsFoldingDescriptors(root);
    if (Objects.nonNull(importsFoldingDescriptors)) {
      importsFoldingDescriptors.setPlaceholderText("imports...");
      foldingDescriptors.add(importsFoldingDescriptors);
    }
    return foldingDescriptors.toArray(new FoldingDescriptor[0]);
  }

  @NotNull
  private List<FoldingDescriptor> getBodyFoldingDescriptors(@NotNull PsiElement psiElement) {
    return PsiTreeUtil.findChildrenOfType(psiElement, LuaBody.class)
        .stream()
        .filter(body -> !StringUtil.isEmpty(body.getText()))
        .map(body -> new FoldingDescriptor(body.getNode(), body.getTextRange()))
        .collect(Collectors.toList());
  }

  private FoldingDescriptor getImportsFoldingDescriptors(@NotNull PsiElement psiElement) {
    var importList = PsiTreeUtil.findChildOfType(psiElement, LuaImportList.class);
    if (Objects.nonNull(importList)) {
      return new FoldingDescriptor(importList.getNode(), importList.getTextRange());
    }
    return null;
  }

  @NotNull
  private List<FoldingDescriptor> getFunctionAssignmentFoldingDescriptors(@NotNull PsiElement psiElement) {
    return PsiTreeUtil.findChildrenOfType(psiElement, LuaFuncAssignment.class)
        .stream()
        .filter(funcAssignment -> !StringUtil.isEmpty(funcAssignment.getText()))
        .map(funcAssignment -> new FoldingDescriptor(funcAssignment.getNode(), funcAssignment.getTextRange()))
        .collect(Collectors.toList());
  }

  @NotNull
  private List<FoldingDescriptor> getTableFoldingDescriptors(@NotNull PsiElement psiElement) {
    return PsiTreeUtil.findChildrenOfType(psiElement, LuaTable.class)
        .stream()
        .filter(table -> !StringUtil.isEmpty(table.getText()))
        .map(table -> new FoldingDescriptor(table.getNode(), table.getTextRange()))
        .collect(Collectors.toList());
  }

  @Nullable
  @Override
  public String getPlaceholderText(@NotNull final ASTNode astNode) {
    return "...";
  }

  @Override
  public boolean isCollapsedByDefault(@NotNull final ASTNode astNode) {
    return false;
  }
}
