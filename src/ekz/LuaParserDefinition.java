package ekz;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.IStubFileElementType;
import com.intellij.psi.tree.TokenSet;
import ekz.parser.LuaParser;
import ekz.psi.LuaElementType;
import ekz.psi.LuaFile;
import ekz.psi.LuaTypes;
import org.jetbrains.annotations.NotNull;

public class LuaParserDefinition implements ParserDefinition {
	public static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);
	public static final TokenSet COMMENTS = TokenSet.create(LuaTypes.SHORT_COMMENT, LuaTypes.BLOCK_COMMENT);

	public static final IStubFileElementType FILE = new IStubFileElementType(Lua.INSTANCE);

	public static IElementType createType(final String typeName) {
		if ("CLASS".equals(typeName)) {
			return LuaElementType.LUA_CLASS;
		} else if ("ID_NAME".equals(typeName)) {
			return LuaElementType.LUA_GVAR;
		} else if ("CLASS_VAR_DEFINITION".equals(typeName)) {
			return LuaElementType.LUA_VAR_BEAN;
		} else {
			return new LuaElementType(typeName);
		}
	}

	@NotNull
	@Override
	public Lexer createLexer(final Project project) {
		return new LuaLexerAdapter();
	}

	@NotNull
	public TokenSet getWhitespaceTokens() {
		return WHITE_SPACES;
	}

	public SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
		return SpaceRequirements.MAY;
	}

	@Override
	public PsiParser createParser(final Project project) {
		return new LuaParser();
	}

	@Override
	public IFileElementType getFileNodeType() {
		return FILE;
	}

	@NotNull
	@Override
	public TokenSet getCommentTokens() {
		return COMMENTS;
	}

	@NotNull
	@Override
	public TokenSet getStringLiteralElements() {
		return TokenSet.EMPTY;
	}

	@NotNull
	@Override
	public PsiElement createElement(final ASTNode astNode) {
		return LuaTypes.Factory.createElement(astNode);
	}

	@Override
	public PsiFile createFile(final FileViewProvider fileViewProvider) {
		return new LuaFile(fileViewProvider);
	}
}
