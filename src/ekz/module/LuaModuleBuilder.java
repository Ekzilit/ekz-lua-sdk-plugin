package ekz.module;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.SourcePathsBuilder;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LuaModuleBuilder extends ModuleBuilder implements SourcePathsBuilder {
	private List<Pair<String, String>> mySourcePaths;

	@Override
	public ModuleType getModuleType() {
		return LuaModuleType.INSTANCE;
	}

	@Override
	public List<Pair<String, String>> getSourcePaths() throws ConfigurationException {
		if (mySourcePaths == null) {
			final List<Pair<String, String>> paths = new ArrayList<>();
			@NonNls final var path = getContentEntryPath() + File.separator + "src";
			new File(path).mkdirs();
			paths.add(Pair.create(path, ""));
			return paths;
		}
		return mySourcePaths;
	}

	@Override
	public void setSourcePaths(List<Pair<String, String>> sourcePaths) {
		mySourcePaths = sourcePaths != null ? new ArrayList<>(sourcePaths) : null;
	}

	@Override
	public void addSourcePath(Pair<String, String> sourcePathInfo) {
		if (mySourcePaths == null) {
			mySourcePaths = new ArrayList<>();
		}
		mySourcePaths.add(sourcePathInfo);
	}

	@Override
	public void setupRootModel(@NotNull ModifiableRootModel rootModel) throws ConfigurationException {
		var contentEntry = doAddContentEntry(rootModel);
		if (contentEntry != null) {
			final var sourcePaths = getSourcePaths();

			if (sourcePaths != null) {
				for (final var sourcePath : sourcePaths) {
					var first = sourcePath.first;
					new File(first).mkdirs();
					final var sourceRoot = LocalFileSystem.getInstance()
							.refreshAndFindFileByPath(FileUtil.toSystemIndependentName(first));
					if (sourceRoot != null) {
						contentEntry.addSourceFolder(sourceRoot, false, sourcePath.second);
					}
				}
			}
		}
	}

}
