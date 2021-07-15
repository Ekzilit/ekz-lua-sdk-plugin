package ekz.psi.stubs;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.psi.tree.TokenSet;
import ekz.Lua;
import ekz.psi.LuaClassVarDefinition;
import ekz.psi.LuaTypes;
import ekz.psi.impl.LuaClassVarDefinitionImpl;
import ekz.psi.stubs.impl.LuaVarBeanStubImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class LuaVarBeanType extends IStubElementType<LuaVarBeanStub, LuaClassVarDefinition> {
  public LuaVarBeanType() {
    super("LUA_VAR_BEAN", Lua.INSTANCE);
  }

  @Override
  public LuaClassVarDefinition createPsi(@NotNull final LuaVarBeanStub varBeanStub) {
    return new LuaClassVarDefinitionImpl(varBeanStub, this);
  }

  @NotNull
  @Override
  public LuaVarBeanStub createStub(@NotNull final LuaClassVarDefinition classVarDefinition,
                                   final StubElement stubElement) {
    return new LuaVarBeanStubImpl(stubElement);
  }

  @NotNull
  @Override
  public String getExternalId() {
    return "lua.var.bean";
  }

  @Override
  public void serialize(@NotNull final LuaVarBeanStub varBeanStub,
                        @NotNull final StubOutputStream stubOutputStream) throws IOException {
  }

  @NotNull
  @Override
  public LuaVarBeanStub deserialize(@NotNull final StubInputStream stubInputStream,
                                    final StubElement stubElement) throws IOException {
    return new LuaVarBeanStubImpl(stubElement);
  }

  @Override
  public void indexStub(@NotNull final LuaVarBeanStub varBeanStub, @NotNull final IndexSink indexSink) {
    indexSink.occurrence(StubKeys.LUA_VAR_BEAN, "bean");
  }

  @Override
  public boolean shouldCreateStub(ASTNode node) {
    final var indicators = node.getChildren(TokenSet.create(LuaTypes.CLASS_VAR_INDICATOR));
    return indicators.length == 1 &&
        indicators[0].getChildren(TokenSet.create(LuaTypes.CLASS_VAR_BEAN_TYPE)).length == 1;
  }
}