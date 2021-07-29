package ekz.psi.stubs;

import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.source.tree.FileElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.psi.tree.TokenSet;
import ekz.Lua;
import ekz.psi.LuaClass;
import ekz.psi.LuaIdName;
import ekz.psi.LuaTypes;
import ekz.psi.impl.LuaIdNameImpl;
import ekz.psi.stubs.impl.LuaGVarStubImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class LuaGVarType extends IStubElementType<LuaGVarStub, LuaIdName> {
	public LuaGVarType() {
		super("LUA_GLOBAL_VAR", Lua.INSTANCE);
	}

	@Override
	public LuaIdName createPsi(@NotNull final LuaGVarStub luaGVarStub) {
		return new LuaIdNameImpl(luaGVarStub, this);
	}

	@NotNull
	@Override
	public LuaGVarStub createStub(@NotNull final LuaIdName luaIdName, final StubElement stubElement) {
		return new LuaGVarStubImpl(stubElement);
	}

	@NotNull
	@Override
	public String getExternalId() {
		return "lua.GVAR";
	}

	@Override
	public void serialize(@NotNull final LuaGVarStub luaGVarStub,
						  @NotNull final StubOutputStream stubOutputStream) throws IOException {
	}

	@Override
	public boolean shouldCreateStub(ASTNode node) {
		return isGlobal(node);
	}

	@NotNull
	@Override
	public LuaGVarStub deserialize(@NotNull final StubInputStream stubInputStream,
								   final StubElement stubElement) throws IOException {
		return new LuaGVarStubImpl(stubElement);
	}

	@Override
	public void indexStub(@NotNull final LuaGVarStub luaGVarStub, @NotNull final IndexSink indexSink) {
		if (Objects.nonNull(luaGVarStub.getParentStubOfType(LuaClass.class))) {
			indexSink.occurrence(StubKeys.GLOBAL_VAR, "CLASS_GVAR");
		} else {
			indexSink.occurrence(StubKeys.GLOBAL_VAR, "GVAR");
		}
	}

	private boolean isGlobal(ASTNode node) {
		var parent = node.getTreeParent();
		if (parent.getElementType() == LuaTypes.TABLE_ELEMENT) {
			return false;
		}
		if (parent.getElementType() == LuaTypes.FUNC_ATTRIBUTES &&
				parent.getTreeParent().getElementType() == LuaTypes.FUNC_ASSIGNMENT) {
			return false;
		}
		if (parent.getTreeParent().getElementType() == LuaTypes.LOCAL_VAR_ASSIGNMENT &&
				parent.getElementType() == LuaTypes.VAR_ID) {
			return false;
		}
		if (parent.getElementType() == LuaTypes.FUNC_DEF_NAME && parent.getTreeParent().getElementType() == LuaTypes.FUNC_DEF &&
				parent.getChildren(TokenSet.create(LuaTypes.ID_NAME)).length == 1) {
			return true;
		}
		if ((parent.getElementType() == LuaTypes.VAR_ID || parent.getElementType() == LuaTypes.FUNC_DEF_NAME) &&
				node.getStartOffset() != parent.getFirstChildNode().getStartOffset()) {
			return false;
		}
		while (!(parent instanceof FileElement)) {
			if (parent.getElementType() == LuaTypes.FUNC_ASSIGNMENT) {
				if (parent.getTreeParent().getElementType() == LuaTypes.VALUE) {
					var funcAssignmentHasLocal = hasLocalWithName(node.getText(), parent.getTreeParent());
					if (funcAssignmentHasLocal) {
						return false;
					}
				}
			} else if (parent.getElementType() == LuaTypes.BODY || parent.getElementType() == LuaTypes.CLASS_METHOD_DEFINITION) {
				var statementHasLocal = hasLocalWithName(node.getText(), parent);
				if (statementHasLocal) {
					return false;
				}
			}
			parent = parent.getTreeParent();
		}
		return !hasLocalWithName(node.getText(), parent);
	}

	private boolean hasLocalWithName(String localVarName, ASTNode parent) {
		return Arrays.stream(parent.getChildren(TokenSet.create(LuaTypes.LOCAL_VAR_ASSIGNMENT)))
				.anyMatch(node1 -> Arrays.stream(node1.getChildren(TokenSet.create(LuaTypes.VAR_ID)))
						.anyMatch(node2 -> Arrays.stream(node2.getChildren(TokenSet.create(LuaTypes.ID_NAME)))
								.anyMatch(node3 -> localVarName.equals(node3.getText())))) ||
				Arrays.stream(parent.getChildren(TokenSet.create(LuaTypes.FUNC_LOCAL_DEF)))
						.anyMatch(node1 -> Arrays.stream(node1.getChildren(TokenSet.create(LuaTypes.FUNC_DEF_NAME)))
								.anyMatch(node2 -> Arrays.stream(node2.getChildren(TokenSet.create(LuaTypes.ID_NAME)))
										.anyMatch(node3 -> localVarName.equals(node3.getText())))) ||
				Arrays.stream(parent.getChildren(TokenSet.create(LuaTypes.FUNC_LOCAL_DEF)))
						.anyMatch(node1 -> Arrays.stream(node1.getChildren(TokenSet.create(LuaTypes.FUNC_ATTRIBUTES)))
								.anyMatch(node2 -> Arrays.stream(node2.getChildren(TokenSet.create(LuaTypes.ID_NAME)))
										.anyMatch(node3 -> localVarName.equals(node3.getText())))) ||
				Arrays.stream(parent.getChildren(TokenSet.create(LuaTypes.FUNC_DEF)))
						.anyMatch(node1 -> Arrays.stream(node1.getChildren(TokenSet.create(LuaTypes.FUNC_ATTRIBUTES)))
								.anyMatch(node2 -> Arrays.stream(node2.getChildren(TokenSet.create(LuaTypes.ID_NAME)))
										.anyMatch(node3 -> localVarName.equals(node3.getText())))) ||
				Arrays.stream(parent.getChildren(TokenSet.create(LuaTypes.FUNC_ASSIGNMENT)))
						.anyMatch(node1 -> Arrays.stream(node1.getChildren(TokenSet.create(LuaTypes.FUNC_ATTRIBUTES)))
								.anyMatch(node2 -> Arrays.stream(node2.getChildren(TokenSet.create(LuaTypes.ID_NAME)))
										.anyMatch(node3 -> localVarName.equals(node3.getText())))) ||
				Arrays.stream(parent.getChildren(TokenSet.create(LuaTypes.FOR_STATEMENT)))
						.anyMatch(node1 -> Arrays.stream(
								node1.getChildren(TokenSet.create(LuaTypes.FOR_P_CONDITION, LuaTypes.FOR_CONDITION)))
								.anyMatch(node2 -> Arrays.stream(node2.getChildren(TokenSet.create(LuaTypes.ID_NAME)))
										.anyMatch(node3 -> localVarName.equals(node3.getText()))));
	}

}
