package ekz.psi.search;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.project.DumbService;
import com.intellij.util.Processor;
import ekz.psi.LuaClass;
import org.jetbrains.annotations.NotNull;

import static ekz.psi.search.LuaClassImplementationsSearchHelper.getChildren;

public class LuaClassImplementationsSearcher extends QueryExecutorBase<LuaClass, LuaClassImplementationsSearch.SearchParameters> {
	@Override
	public void processQuery(@NotNull LuaClassImplementationsSearch.SearchParameters searchParameters,
							 @NotNull Processor<? super LuaClass> processor) {

		DumbService.getInstance(searchParameters.getLuaClassName().getProject()).runReadActionInSmartMode(() -> {
			for (var luaClass : getChildren(searchParameters.getLuaClassName())) {
				processor.process(luaClass);
			}
			return true;
		});
	}
}
