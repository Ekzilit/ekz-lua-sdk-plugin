package ekz;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static ekz.psi.LuaTypes.*;

%%

%{
  public _LuaLexer() {
    this((java.io.Reader)null);
  }
      private int nBrackets = 0;
      private boolean checkAhead(char c, int offset) {
          return this.zzMarkedPos + offset < this.zzBuffer.length() && this.zzBuffer.charAt(this.zzMarkedPos + offset) == c;
      }

      private boolean checkBlock() {
          nBrackets = 0;
          if (checkAhead('[', 0)) {
              int n = 0;
              while (checkAhead('=', n + 1)) n++;
              if (checkAhead('[', n + 1)) {
                  nBrackets = n;
                  return true;
              }
          }
          return false;
      }

       private int checkBlockRedundant() {
              int redundant = -1;
              String cs = yytext().toString();
              StringBuilder s = new StringBuilder("]");
              for (int i = 0; i < nBrackets; i++) s.append('=');
              s.append(']');
              int index = cs.indexOf(s.toString());
              if (index > 0)
                  redundant = yylength() - index - nBrackets - 2;
              return redundant;
          }
%}

%public
%class _LuaLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

EOL=\R
WHITE_SPACE=\s+

BLOCK_COMMENT=--\[=*\[[\s\S]*(\]=*\])?
SHORT_COMMENT=--[^\r\n]*
ID=[A-Za-z_][A-Za-z0-9_]*
//Number
n=[0-9]+
h=[0-9a-fA-F]+
exp=[Ee]([+-]?{n})?
ppp=[Pp][+-]{n}
NUMBER=(0[xX]({h}|{h}[.]{h})({exp}|{ppp})?|({n}|{n}[.]{n}){exp}?|[.]{n}|{n}[.])
SPACE=[ \t\n\x0B\f\r]+
//Strings
DOUBLE_QUOTED_STRING=\"([^\\\"]|\\\S|\\[\r\n])*\"?  //\"([^\\\"\r\n]|\\[^\r\n])*\"?
SINGLE_QUOTED_STRING='([^\\\']|\\\S|\\[\r\n])*'?    //'([^\\'\r\n]|\\[^\r\n])*'?
//[[]]
LONG_STRING=\[=*\[[\s\S]*\]=*\]

%state xBLOCK_COMMENT
%state xCOMMENT
%state xDOUBLE_QUOTED_STRING
%state xSINGLE_QUOTED_STRING
%state xBLOCK_STRING

%%
<YYINITIAL> {
  {WHITE_SPACE}               { return WHITE_SPACE; }
  "--"                        {
        boolean block = checkBlock();
        if (block) { yypushback(yylength()); yybegin(xBLOCK_COMMENT); }
        else { yypushback(yylength()); yybegin(xCOMMENT); }
   }
  "and"                       { return AND; }
  "break"                     { return BREAK; }
  "do"                        { return DO; }
  "else"                      { return ELSE; }
  "elseif"                    { return ELSEIF; }
  "end"                       { return END; }
  "false"                     { return FALSE; }
  "for"                       { return FOR; }
  "function"                  { return FUNC; }
  "if"                        { return IF; }
  "in"                        { return IN; }
  "local"                     { return LOCAL; }
  "nil"                       { return NIL; }
  "not"                       { return NOT; }
  "or"                        { return OR; }
  "repeat"                    { return REPEAT; }
  "return"                    { return RETURN; }
  "then"                      { return THEN; }
  "true"                      { return TRUE; }
  "until"                     { return UNTIL; }
  "while"                     { return WHILE; }
  "REGION"                    { return REGION; }
  "ENDREGION"                 { return ENDREGION; }
  "..."                       { return ELLIPSIS; }
  ".."                        { return CONCAT; }
  "=="                        { return EQ; }
  ">="                        { return GE; }
  "<="                        { return LE; }
  "~="                        { return NE; }
  "-"                         { return MINUS; }
  "+"                         { return PLUS; }
  "*"                         { return MULT; }
  "%"                         { return MOD; }
  "/"                         { return DIV; }
  "="                         { return ASSIGN; }
  ">"                         { return GT; }
  "<"                         { return LT; }
  "("                         { return LPAREN; }
  ")"                         { return RPAREN; }
  "["                         { return LBRACK; }
  "]"                         { return RBRACK; }
  "{"                         { return LCURLY; }
  "}"                         { return RCURLY; }
  "#"                         { return GETN; }
  ","                         { return COMMA; }
  ";"                         { return SEMI; }
  ":"                         { return COLON; }
  "."                         { return DOT; }
  "^"                         { return EXP; }
  "_"                         { return EVAR; }
        "\""                        { yybegin(xDOUBLE_QUOTED_STRING); yypushback(yylength()); }
        "'"                         { yybegin(xSINGLE_QUOTED_STRING); yypushback(yylength()); }
        \[=*\[                      { yybegin(xBLOCK_STRING); yypushback(yylength()); checkBlock(); }

  {ID}                        { return ID; }
  {NUMBER}                    { return NUMBER; }

[^] { return BAD_CHARACTER; }
}


<xCOMMENT> {
    {SHORT_COMMENT}           {yybegin(YYINITIAL);return SHORT_COMMENT;}
}

<xBLOCK_COMMENT> {
    {BLOCK_COMMENT}           {
        int redundant = checkBlockRedundant();
        if (redundant != -1) {
            yypushback(redundant);
            yybegin(YYINITIAL);return BLOCK_COMMENT; }
        else { yybegin(YYINITIAL);return BLOCK_COMMENT; }
    }
    [^] { yypushback(yylength()); yybegin(xCOMMENT); }
}

<xDOUBLE_QUOTED_STRING> {
    {DOUBLE_QUOTED_STRING}    { yybegin(YYINITIAL); return STRING; }
}

<xSINGLE_QUOTED_STRING> {
    {SINGLE_QUOTED_STRING}    { yybegin(YYINITIAL); return STRING; }
}

<xBLOCK_STRING> {
    {LONG_STRING}             {
        int redundant = checkBlockRedundant();
        if (redundant != -1) {
            yypushback(redundant);
            yybegin(YYINITIAL); return STRING;
        } else {
            yybegin(YYINITIAL); return BAD_CHARACTER;
        }
    }
    [^] { return BAD_CHARACTER; }
}