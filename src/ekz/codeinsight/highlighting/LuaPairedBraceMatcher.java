package ekz.codeinsight.highlighting;

import com.intellij.codeInsight.highlighting.PairedBraceMatcherAdapter;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.tree.IElementType;
import ekz.Lua;

import java.util.Objects;

public class LuaPairedBraceMatcher extends PairedBraceMatcherAdapter {
	public LuaPairedBraceMatcher() {
		super(new LuaBraceMatcher(), Lua.INSTANCE);
	}

	public boolean isLBraceTokenOfRBrace(HighlighterIterator iterator, CharSequence fileText, FileType fileType,
										 IElementType rBraceType) {
		var bracePair = findPair(true, iterator, fileText, fileType);
		return Objects.nonNull(bracePair) && bracePair.getRightBraceType() == rBraceType;
	}

	public boolean isRBraceTokenOfRBrace(HighlighterIterator iterator, CharSequence fileText, FileType fileType,
										 IElementType rBraceType) {
		var bracePair = findPair(false, iterator, fileText, fileType);
		return Objects.nonNull(bracePair) && bracePair.getRightBraceType() == rBraceType;
	}
}
