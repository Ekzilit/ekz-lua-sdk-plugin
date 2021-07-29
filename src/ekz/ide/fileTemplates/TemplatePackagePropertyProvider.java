package ekz.ide.fileTemplates;

import com.intellij.ide.fileTemplates.DefaultTemplatePropertiesProvider;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

public class TemplatePackagePropertyProvider implements DefaultTemplatePropertiesProvider {
	@Override
	public void fillProperties(@NotNull PsiDirectory directory, @NotNull Properties props) {
		var projectFileIndex = ProjectRootManager.getInstance(directory.getProject()).getFileIndex();
		var virtualFile = directory.getVirtualFile();
		var packageName = projectFileIndex.getPackageNameByDirectory(virtualFile);
		if (packageName != null) {
			props.setProperty(FileTemplate.ATTRIBUTE_PACKAGE_NAME, packageName);
		}
	}
}
