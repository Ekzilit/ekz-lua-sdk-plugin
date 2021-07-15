package ekz.psi.search;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.search.searches.ExtensibleQueryFactory;
import com.intellij.util.Query;
import com.intellij.util.QueryExecutor;
import com.intellij.util.QueryParameters;
import ekz.psi.LuaClassElementDefinition;

public class LuaClassElementImplementationsSearch extends ExtensibleQueryFactory<LuaClassElementDefinition, LuaClassElementImplementationsSearch.SearchParameters> {
	public static final ExtensionPointName<QueryExecutor> EP_NAME = ExtensionPointName.create(
			"ekz.luaClassElementImplementationsSearch");
	public static final LuaClassElementImplementationsSearch INSTANCE = new LuaClassElementImplementationsSearch();

	public LuaClassElementImplementationsSearch() {
		super(ExtensionPointName.create("ekz"));
	}

	public static Query<LuaClassElementDefinition> search(
			LuaClassElementImplementationsSearch.SearchParameters searchParameters) {
		return INSTANCE.createUniqueResultsQuery(searchParameters);
	}

	public static class SearchParameters implements QueryParameters {
		private final LuaClassElementDefinition classElementDefinition;

		public SearchParameters(final LuaClassElementDefinition luaTableElement) {
			this.classElementDefinition = luaTableElement;
		}

		public LuaClassElementDefinition getClassElementDefinition() {
			return classElementDefinition;
		}
	}
}
