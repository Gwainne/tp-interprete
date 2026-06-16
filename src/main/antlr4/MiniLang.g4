grammar MiniLang;

// ══════════════════════════════════════════════════════
//  PARSER RULES
// ══════════════════════════════════════════════════════

program
    : statement* EOF
    ;

statement
    : declaration
    | assignment
    | printStmt
    | ifStmt
    | repeatStmt
    | block
    ;

block
    : '{' statement* '}'
    ;

declaration
    : type ID '=' expr ';'
    ;

assignment
    : ID '=' expr ';'
    ;

type
    : 'int'
    | 'float'
    | 'string'
    | 'bool'
    ;

printStmt
    : 'print' '(' expr ')' ';'
    ;

ifStmt
    : 'if' '(' expr ')' block ('else' block)?
    ;

repeatStmt
    : 'repeat' block 'until' '(' expr ')' ';'
    ;

// ── Expresiones con precedencia de mayor a menor ──────
expr
    : '!' expr                                          # Not
    | expr ('*' | '/') expr                             # MulDiv
    | expr ('+' | '-') expr                             # AddSub
    | expr ('<' | '>' | '<=' | '>=') expr              # Relational
    | expr ('==' | '!=') expr                           # Equality
    | expr '&&' expr                                    # And
    | expr '||' expr                                    # Or
    | '(' expr ')'                                      # Parens
    | INT                                               # IntLit
    | FLOAT                                             # FloatLit
    | STRING                                            # StringLit
    | BOOL                                              # BoolLit
    | ID                                                # Var
    ;

// ══════════════════════════════════════════════════════
//  LEXER RULES
// ══════════════════════════════════════════════════════

// ── Literales ─────────────────────────────────────────
FLOAT   : [0-9]+ '.' [0-9]+ ;
INT     : [0-9]+ ;
STRING  : '"' (~["\r\n])* '"' ;
BOOL    : 'true' | 'false' ;

// ── Identificadores ───────────────────────────────────
ID      : [a-zA-Z_][a-zA-Z_0-9]* ;

// ── Ignorados ─────────────────────────────────────────
COMMENT : '//' ~[\r\n]* -> skip ;
WS      : [ \t\r\n]+    -> skip ;