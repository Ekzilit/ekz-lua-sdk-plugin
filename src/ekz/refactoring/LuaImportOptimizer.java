package ekz.refactoring;

import com.intellij.lang.ImportOptimizer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import ekz.psi.LuaElementFactory;
import ekz.psi.LuaFile;
import ekz.psi.LuaImportList;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

public class LuaImportOptimizer implements ImportOptimizer {
	private static final Logger LOG = Logger.getInstance("#ekz.refactoring.LuaImportOptimizer");

	@Override
	public boolean supports(PsiFile file) {
		return file instanceof LuaFile;
	}

	@NotNull
	@Override
	public Runnable processFile(PsiFile file) {
		return new CollectingInfoRunnable() {
			private boolean sorted;

			@Override
			public void run() {
				try {
					final var oldImportList = PsiTreeUtil.getChildOfType(file.getFirstChild(), LuaImportList.class);
					if (Objects.nonNull(oldImportList)) {
						final var sortedImportDefinitions = oldImportList.getImportDefinitionList()
								.stream()
								.sorted(Comparator.comparing(
										luaImportDefinition -> luaImportDefinition.getClassNameWithPath().getUnquotedText()))
								.collect(Collectors.toList());
						oldImportList.replace(
								LuaElementFactory.createImportListElement(file.getProject(), sortedImportDefinitions));
						for (var i = 0; i < oldImportList.getImportDefinitionList().size(); i++) {
							if (!sortedImportDefinitions.get(i)
									.getText()
									.equals(oldImportList.getImportDefinitionList().get(i).getText())) {
								sorted = true;
								break;
							}
						}
					}
				} catch (IncorrectOperationException e) {
					LOG.error(e);
				}
			}

			@Override
			public String getUserNotificationInfo() {
				if (sorted) {
					return "rearranged imports";
				}
				return null;
			}
		};
	}
}
