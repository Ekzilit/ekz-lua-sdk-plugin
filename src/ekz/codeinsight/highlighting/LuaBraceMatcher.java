package ekz.codeinsight.highlighting;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import ekz.psi.LuaIfStatement;
import ekz.psi.LuaTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LuaBraceMatcher implements PairedBraceMatcher {

	private final BracePair[] pairs = new BracePair[]{new BracePair(LuaTypes.LPAREN, LuaTypes.RPAREN, false),
			new BracePair(LuaTypes.LCURLY, LuaTypes.RCURLY, true), new BracePair(LuaTypes.LBRACK, LuaTypes.RBRACK, false),
			new BracePair(LuaTypes.FUNC, LuaTypes.END, true), new BracePair(LuaTypes.DO, LuaTypes.END, true),
			new BracePair(LuaTypes.REPEAT, LuaTypes.UNTIL, true), new BracePair(LuaTypes.IF, LuaTypes.END, true)
	};

	@NotNull
	@Override
	public BracePair[] getPairs() {
		return pairs;
	}

	@Override
	public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType contextType) {
		return true;
	}

	@Override
	public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
		var element = file.findElementAt(openingBraceOffset);
		if (element == null || element instanceof PsiFile) return openingBraceOffset;
		var parent = element.getParent();
		if (parent instanceof LuaIfStatement) {
			var range = parent.getParent().getTextRange();
			return range.getStartOffset();
		}
		return openingBraceOffset;
	}
}
