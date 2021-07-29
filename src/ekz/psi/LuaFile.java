package ekz.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.util.PsiTreeUtil;
import ekz.Lua;
import ekz.LuaFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class LuaFile extends PsiFileBase {
	public LuaFile(@NotNull final FileViewProvider viewProvider) {
		super(viewProvider, Lua.INSTANCE);
	}

	@NotNull
	@Override
	public FileType getFileType() {
		return LuaFileType.INSTANCE;
	}

	@Override
	public String toString() {
		return "Lua File";
	}

	@Nullable
	@Override
	public Icon getIcon(final int flags) {
		return super.getIcon(flags);
	}

	@NotNull
	@Override
	public String getName() {
		return super.getName();
	}

	public String getNameWithPackage() {
		var packageDefinitions = PsiTreeUtil.getChildrenOfType(this, LuaClassPackageDefinition.class);
		if (Objects.nonNull(packageDefinitions) && packageDefinitions.length > 0) {
			final var fileName = super.getName();
			return fileName.substring(0, fileName.length() - 4) + "(" +
					packageDefinitions[0].getClassPackage().getUnquotedText() + ")";
		}
		return super.getName();
	}
}
