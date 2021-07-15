package ekz.codeinsight.daemon;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.codeInsight.daemon.impl.LineMarkersPass;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.psi.PsiElement;
import ekz.psi.LuaClassElementDefinition;
import ekz.psi.LuaClassMethodDefinition;
import ekz.psi.LuaClassVarDefinition;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class LuaLineMarkerProvider extends LineMarkerProviderDescriptor {
  @Nls(capitalization = Nls.Capitalization.Sentence)
  @Nullable("null means disabled")
  @Override
  public String getName() {
    return "Lua line markers";
  }

  @Nullable
  @Override
  public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
    if (element instanceof LuaClassElementDefinition) {
      if (element.getFirstChild() instanceof LuaClassVarDefinition) {
        var classElementName = ((LuaClassVarDefinition) element.getFirstChild()).getClassVarName();
        if (Objects.nonNull(classElementName)) {
          return LineMarkersPass.createMethodSeparatorLineMarker(classElementName.getFirstChild(),
              EditorColorsManager.getInstance());
        }

      }
      if (element.getFirstChild() instanceof LuaClassMethodDefinition) {
        var classElementName = ((LuaClassMethodDefinition) element.getFirstChild()).getClassMethodName();
        if (Objects.nonNull(classElementName)) {
          return LineMarkersPass.createMethodSeparatorLineMarker(classElementName.getFirstChild(),
              EditorColorsManager.getInstance());
        }

      }
    }
    return null;
  }
}
