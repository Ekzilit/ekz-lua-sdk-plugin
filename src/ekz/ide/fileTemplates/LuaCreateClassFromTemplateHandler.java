package ekz.ide.fileTemplates;

import com.intellij.ide.fileTemplates.CreateFromTemplateHandler;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import ekz.LuaFileType;
import ekz.psi.LuaClass;
import ekz.psi.LuaClassHeader;
import ekz.psi.LuaClassName;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class LuaCreateClassFromTemplateHandler implements CreateFromTemplateHandler {
	@Override
	public boolean handlesTemplate(@NotNull FileTemplate template) {
		var fileType = FileTypeManagerEx.getInstanceEx().getFileTypeByExtension(template.getExtension());
		return fileType.equals(LuaFileType.INSTANCE) && !"LuaContext".equals(template.getName());
	}

	@NotNull
	@Override
	public PsiElement createFromTemplate(@NotNull Project project, @NotNull PsiDirectory directory, String fileName,
										 @NotNull FileTemplate template, @NotNull String templateText,
										 @NotNull Map<String, Object> props) throws IncorrectOperationException {
		final var name = "myClass.lua";
		var psiFile = PsiFileFactory.getInstance(project).createFileFromText(name, LuaFileType.INSTANCE, templateText);
		var classes = PsiTreeUtil.getChildrenOfAnyType(psiFile, LuaClass.class);
		psiFile.setName(
				PsiTreeUtil.getChildrenOfAnyType(PsiTreeUtil.getChildrenOfAnyType(classes.get(0), LuaClassHeader.class).get(0),
						LuaClassName.class).get(0).getText().replaceAll("\"", "") + ".lua");
		return directory.add(psiFile);
	}

	@Override
	public boolean canCreate(@NotNull PsiDirectory[] dirs) {
		return false;
	}

	@Override
	public boolean isNameRequired() {
		return false;
	}

	@NotNull
	@Override
	public String getErrorMessage() {
		return "Some error in file creation happens";
	}

	@Override
	public void prepareProperties(@NotNull Map<String, Object> props) {

	}
}
