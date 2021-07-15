package ekz.psi.search;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.search.searches.ExtensibleQueryFactory;
import com.intellij.util.Query;
import com.intellij.util.QueryExecutor;
import com.intellij.util.QueryParameters;
import com.intellij.util.containers.ContainerUtil;
import ekz.psi.LuaClass;
import ekz.psi.LuaClassName;

public class LuaClassImplementationsSearch extends ExtensibleQueryFactory<LuaClass, LuaClassImplementationsSearch.SearchParameters> {
  public static final ExtensionPointName<QueryExecutor> EP_NAME = ExtensionPointName.create(
      "ekz.luaClassImplementationsSearch");
  public static final LuaClassImplementationsSearch INSTANCE = new LuaClassImplementationsSearch();

  public LuaClassImplementationsSearch() {
    super(ExtensionPointName.create("ekz"));
  }

  public static Query<LuaClass> search(LuaClassImplementationsSearch.SearchParameters searchParameters) {
    return INSTANCE.createUniqueResultsQuery(searchParameters);
  }

  public static class SearchParameters implements QueryParameters {
    private final LuaClassName luaClassName;

    public SearchParameters(LuaClassName luaClassName) {
      this.luaClassName = luaClassName;
    }

    public LuaClassName getLuaClassName() {
      return luaClassName;
    }
  }
}
