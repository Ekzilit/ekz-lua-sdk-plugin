package ekz.ide.action;

import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.ide.actions.CreateTemplateInPackageAction;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.ui.CreateFromTemplateDialog;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidatorEx;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameHelper;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.PlatformIcons;
import ekz.psi.LuaClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes;

import java.util.Properties;

public class CreateClassAction extends CreateTemplateInPackageAction<PsiElement> implements DumbAware {
	public CreateClassAction() {
		super("Lua Class", "Create new Lua class", PlatformIcons.CLASS_ICON, JavaModuleSourceRootTypes.SOURCES);
	}

	@Nullable
	@Override
	protected PsiElement getNavigationElement(@NotNull PsiElement createdElement) {
		return null;
	}

	@Override
	protected boolean checkPackageExists(PsiDirectory directory) {
		return true;
	}

	@Nullable
	@Override
	protected PsiElement doCreate(PsiDirectory dir, String className, String templateName) throws IncorrectOperationException {
		var template = FileTemplateManager.getInstance(dir.getProject()).getInternalTemplate(templateName);
		var defaultProperties = FileTemplateManager.getInstance(dir.getProject()).getDefaultProperties();
		var properties = new Properties(defaultProperties);
		if ("LuaContext".equals(templateName) && className.contains("Context")) {
			className = className.substring(0, className.indexOf("Context")).toLowerCase();
		}
		properties.setProperty(FileTemplate.ATTRIBUTE_NAME, className);
		return PsiTreeUtil.getChildrenOfAnyType(
				new CreateFromTemplateDialog(dir.getProject(), dir, template, null, properties).create(), LuaClass.class).get(0);
	}

	@Override
	protected void buildDialog(Project project, PsiDirectory directory, CreateFileFromTemplateDialog.Builder builder) {
		builder.setTitle("New Lua Class")
				.addKind("Class", PlatformIcons.CLASS_ICON, "LuaClass")
				.addKind("Interface", PlatformIcons.INTERFACE_ICON, "LuaInterface")
				.addKind("Enum", PlatformIcons.ENUM_ICON, "LuaEnum");

		builder.setValidator(new InputValidatorEx() {
			@Override
			public String getErrorText(String inputString) {
				//TODO redo on lua helper
				if (inputString.length() > 0 && !PsiNameHelper.getInstance(project).isQualifiedName(inputString)) {
					return "This is not a valid Lua qualified name";
				}
				return null;
			}

			@Override
			public boolean checkInput(String inputString) {
				return true;
			}

			@Override
			public boolean canClose(String inputString) {
				return !StringUtil.isEmptyOrSpaces(inputString) && getErrorText(inputString) == null;
			}
		});

	}

	@Override
	protected String getActionName(PsiDirectory directory, @NotNull String newName, String templateName) {
		return "Creating class";
	}

	@Override
	protected String removeExtension(String templateName, String className) {
		return StringUtil.trimEnd(className, ".lua");
	}
}
