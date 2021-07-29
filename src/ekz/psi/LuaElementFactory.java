package ekz.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import ekz.LuaFileType;

import java.util.List;

public class LuaElementFactory {
	public static LuaNamedElement createNamedElement(Project project, String name) {
		final var file = createFile(project, name);
		return (LuaNamedElement) file.getFirstChild().getFirstChild();
	}

	public static PsiElement createStringElement(Project project, String name) {
		final var file = createFile(project, name);
		return file.getFirstChild();
	}

	public static PsiElement createCRLF(Project project) {
		final var file = createFile(project, "\n");
		return file.getFirstChild();
	}

	public static LuaImportDefinition createImportElement(Project project, String name) {
		final var file = createFile(project, "package\"\"\n import \"" + name + "\"\n public.class(\"test\"){}");
		return PsiTreeUtil.findChildOfType(file.getFirstChild(), LuaImportDefinition.class);
	}

	public static PsiElement createImportListElement(Project project, List<LuaImportDefinition> importDefinitions) {
		final var file = createFile(project, "package\"\"\n import \"\"\n public.class(\"test\"){}");
		var luaClass = (LuaClass) file.getFirstChild();
		var importList = luaClass.getImportList();
		importList.getFirstChild().delete();
		for (var i = 0; i < importDefinitions.size(); i++) {
			importList.add(importDefinitions.get(i));
			if (i != importDefinitions.size() - 1) {
				importList.add(createCRLF(project));
			}
		}
		return PsiTreeUtil.findChildOfType(luaClass, LuaImportList.class);
	}

	public static LuaFile createFile(Project project, String text) {
		var name = "dummy.lua";
		return (LuaFile) PsiFileFactory.getInstance(project).createFileFromText(name, LuaFileType.INSTANCE, text);
	}
}
