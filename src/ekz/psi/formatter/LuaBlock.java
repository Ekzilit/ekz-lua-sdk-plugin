package ekz.psi.formatter;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.formatting.Wrap;
import com.intellij.formatting.WrapType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.TokenType;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.FormatterUtil;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import ekz.Lua;
import ekz.psi.LuaFile;
import ekz.psi.LuaTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class LuaBlock extends AbstractBlock {
  private static final List<IElementType> ELEMENTS_WITH_INDENT = Arrays.asList(LuaTypes.CLASS_ELEMENT_DEFINITION,
      LuaTypes.TABLE_ELEMENT, LuaTypes.SHORT_COMMENT, LuaTypes.BLOCK_COMMENT, LuaTypes.FUNC_DEF, LuaTypes.FUNC_LOCAL_DEF,
      LuaTypes.IF_STATEMENT, LuaTypes.WHILE_STATEMENT, LuaTypes.REPEAT_STATEMENT, LuaTypes.BEAN_DEFINITION,
      LuaTypes.FOR_STATEMENT, LuaTypes.VAR_ASSIGNMENT, LuaTypes.RETURN_CONDITION, LuaTypes.BREAK, LuaTypes.LOCAL_VAR_ASSIGNMENT,
      TokenType.WHITE_SPACE, LuaTypes.DO_BLOCK);
  private SpacingBuilder spacingBuilder;
  private CodeStyleSettings settings;

  protected LuaBlock(@NotNull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment alignment, SpacingBuilder spacingBuilder,
                     CodeStyleSettings settings) {
    super(node, wrap, alignment);
    this.spacingBuilder = spacingBuilder;
    this.settings = settings;
  }

  @Override
  protected List<Block> buildChildren() {
    List<Block> blocks = new ArrayList<>();
    var child = myNode.getFirstChildNode();
    createBlocks(blocks, child);
    return blocks;
  }

  private void createBlocks(List<Block> blocks, ASTNode child) {
    while (child != null) {
      if (!FormatterUtil.containsWhiteSpacesOnly(child) && child.getTextLength() > 0) {
        var alignment = shouldNotHasAlignment(child) ? null : Alignment.createAlignment();
        Block block = new LuaBlock(child, Wrap.createWrap(WrapType.NONE, false), alignment, getSpacingBuilder(child), settings);
        blocks.add(block);
      }
      child = child.getTreeNext();
    }
  }

  private SpacingBuilder getSpacingBuilder(final ASTNode element) {
    if (element.getElementType() == LuaTypes.VALUES && Arrays.stream(element.getChildren(TokenSet.create(LuaTypes.VALUE)))
        .anyMatch(node -> node.getFirstChildNode().getElementType() == LuaTypes.TABLE)) {
      return new SpacingBuilder(settings, Lua.INSTANCE).between(LuaTypes.COMMA, LuaTypes.VALUE)
          .lineBreakOrForceSpace(false, true);
    }
    return new SpacingBuilder(settings, Lua.INSTANCE).append(spacingBuilder);
  }

  private boolean shouldNotHasAlignment(ASTNode child) {
    return (child.getElementType() == LuaTypes.DO_BLOCK && (child.getTreeParent().getElementType() == LuaTypes.FOR_STATEMENT ||
        child.getTreeParent().getElementType() == LuaTypes.WHILE_STATEMENT)) || child.getElementType() == LuaTypes.CLASS_BODY ||
        child.getElementType() == LuaTypes.TABLE || child.getElementType() == LuaTypes.FUNC_ASSIGNMENT ||
        child.getElementType() == LuaTypes.VALUE || child.getElementType() == LuaTypes.VALUES ||
        child.getElementType() == LuaTypes.FUNC_PARAMS || child.getTreeParent().getPsi() instanceof LuaFile;
  }

  @Nullable
  @Override
  public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
    return spacingBuilder.getSpacing(this, child1, child2);
  }

  @Override
  public boolean isLeaf() {
    return myNode.getFirstChildNode() == null;
  }

  @Override
  public Indent getIndent() {
    if (Objects.nonNull(myNode.getTreeParent()) &&
        (Stream.of(LuaTypes.VALUES, LuaTypes.VALUE, LuaTypes.VAR_ID, LuaTypes.FUNC_PARAMS)
            .anyMatch(iElementType -> myNode.getTreeParent().getElementType() == iElementType))) {
      return Indent.getContinuationWithoutFirstIndent();
    }
    if (shouldHasIndent()) {
      return Indent.getIndent(Indent.Type.NORMAL, false, false);
    }
    return Indent.getNoneIndent();
  }

  private boolean shouldHasIndent() {
    return !isDirectChildOfLuaFile() && isElementWithIndent();
  }

  private boolean isElementWithIndent() {
    return ELEMENTS_WITH_INDENT.contains(myNode.getElementType()) || (isNotVarIdInVarAssignment() && isNotVarIdInTableElement());
  }

  private boolean isDirectChildOfLuaFile() {
    return Objects.nonNull(myNode.getTreeParent()) && myNode.getTreeParent().getPsi() instanceof LuaFile;
  }

  private boolean isNotVarIdInTableElement() {
    return myNode.getElementType() == LuaTypes.VAR_ID && Objects.nonNull(myNode.getTreeParent().getTreeParent()) &&
        myNode.getTreeParent().getTreeParent().getElementType() != LuaTypes.TABLE_ELEMENT;
  }

  private boolean isNotVarIdInVarAssignment() {
    return myNode.getElementType() == LuaTypes.VAR_ID && myNode.getTreeParent().getElementType() != LuaTypes.VAR_ASSIGNMENT;
  }
}
