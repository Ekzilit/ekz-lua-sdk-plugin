package ekz.codeinsight.daemon;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.codeInsight.daemon.impl.LineMarkersPass;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.psi.PsiElement;
import ekz.psi.LuaClassElementDefinition;
import ekz.psi.LuaClassMethodName;
import ekz.psi.LuaClassVarName;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LuaLineMarkerProvider extends LineMarkerProviderDescriptor {
	@Nls(capitalization = Nls.Capitalization.Sentence)
	@Nullable("null means disabled")
	@Override
	public String getName() {
		return "Lua line markers";
	}

	@Nullable
	@Override
	public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
		if (element instanceof LuaClassElementDefinition) {
			if (element.getFirstChild() instanceof LuaClassVarName) {
				return LineMarkersPass.createMethodSeparatorLineMarker(element.getFirstChild(),
						EditorColorsManager.getInstance());
			}
			if (element.getFirstChild() instanceof LuaClassMethodName) {
				return LineMarkersPass.createMethodSeparatorLineMarker(element.getFirstChild(),
						EditorColorsManager.getInstance());
			}
		}
		return null;
	}
}
