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
import ekz.psi.LuaContextDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes;

import java.util.Properties;

public class CreateContextAction extends CreateTemplateInPackageAction<PsiElement> implements DumbAware {
	public CreateContextAction() {
		super("Lua Context", "Create new Lua Context", PlatformIcons.ABSTRACT_CLASS_ICON, JavaModuleSourceRootTypes.SOURCES);
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
		properties.setProperty("originalFileName", className);
		className = className.substring(0, className.indexOf("Context")).toLowerCase();
		properties.setProperty(FileTemplate.ATTRIBUTE_NAME, className);
		return PsiTreeUtil.getChildrenOfAnyType(
				new CreateFromTemplateDialog(dir.getProject(), dir, template, null, properties).create(),
				LuaContextDefinition.class).get(0);
	}

	@Override
	protected void buildDialog(Project project, PsiDirectory directory, CreateFileFromTemplateDialog.Builder builder) {
		builder.setTitle("Create new context").addKind("Context", PlatformIcons.ABSTRACT_CLASS_ICON, "LuaContext");
		builder.setValidator(new InputValidatorEx() {
			@Override
			public String getErrorText(String inputString) {
				//TODO redo on lua helper
				if (inputString.length() > 0 && !PsiNameHelper.getInstance(project).isQualifiedName(inputString) &&
						!inputString.contains("Context")) {
					return "This is not a valid Lua context qualified name";
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
		return "Creating context";
	}

	@Override
	protected String removeExtension(String templateName, String className) {
		return StringUtil.trimEnd(className, ".lua");
	}
}
