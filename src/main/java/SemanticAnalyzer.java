public class SemanticAnalyzer extends MiniLangBaseVisitor<String> {

    private final SymbolTable symbolTable = new SymbolTable();

    // ── Block ─────────────────────────────────────────────────────────────────

    @Override
    public String visitBlock(MiniLangParser.BlockContext ctx) {
        symbolTable.pushScope();
        for (MiniLangParser.StatementContext stmt : ctx.statement()) {
            visit(stmt);
        }
        symbolTable.popScope();
        return null;
    }

    // ── Declaración ──────────────────────────────────────────────────────────

    @Override
    public String visitDeclaration(MiniLangParser.DeclarationContext ctx) {
        String type = ctx.type().getText();
        String name = ctx.ID().getText();
        int line = ctx.getStart().getLine();

        String exprType = visit(ctx.expr());
        checkTypeCompatibility(type, exprType, line);
        symbolTable.declare(name, type, line);

        return null;
    }

    // ── Asignación ───────────────────────────────────────────────────────────

    @Override
    public String visitAssignment(MiniLangParser.AssignmentContext ctx) {
        String name = ctx.ID().getText();
        int line = ctx.getStart().getLine();

        SymbolTable.Symbol symbol = symbolTable.get(name, line);
        String exprType = visit(ctx.expr());
        checkTypeCompatibility(symbol.type, exprType, line);

        return null;
    }

    // ── Print ─────────────────────────────────────────────────────────────────

    @Override
    public String visitPrintStmt(MiniLangParser.PrintStmtContext ctx) {
        visit(ctx.expr());
        return null;
    }

    // ── If ───────────────────────────────────────────────────────────────────

    @Override
    public String visitIfStmt(MiniLangParser.IfStmtContext ctx) {
        int line = ctx.getStart().getLine();
        String condType = visit(ctx.expr());

        if (!condType.equals("bool")) {
            throw new SemanticException(
                "La condición del 'if' debe ser bool, se encontró: " + condType, line);
        }

        for (MiniLangParser.BlockContext block : ctx.block()) {
            visit(block);
        }

        return null;
    }

    // ── Repeat-Until ─────────────────────────────────────────────────────────

    @Override
    public String visitRepeatStmt(MiniLangParser.RepeatStmtContext ctx) {
        int line = ctx.getStart().getLine();

        visit(ctx.block());

        String condType = visit(ctx.expr());
        if (!condType.equals("bool")) {
            throw new SemanticException(
                "La condición del 'until' debe ser bool, se encontró: " + condType, line);
        }

        return null;
    }

    // ── Expresiones ───────────────────────────────────────────────────────────

    @Override
    public String visitIntLit(MiniLangParser.IntLitContext ctx) {
        return "int";
    }

    @Override
    public String visitFloatLit(MiniLangParser.FloatLitContext ctx) {
        return "float";
    }

    @Override
    public String visitStringLit(MiniLangParser.StringLitContext ctx) {
        return "string";
    }

    @Override
    public String visitBoolLit(MiniLangParser.BoolLitContext ctx) {
        return "bool";
    }

    @Override
    public String visitVar(MiniLangParser.VarContext ctx) {
        String name = ctx.ID().getText();
        int line = ctx.getStart().getLine();
        return symbolTable.get(name, line).type;
    }

    @Override
    public String visitParens(MiniLangParser.ParensContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public String visitNot(MiniLangParser.NotContext ctx) {
        int line = ctx.getStart().getLine();
        String type = visit(ctx.expr());
        if (!type.equals("bool")) {
            throw new SemanticException(
                "El operador '!' solo aplica a bool, se encontró: " + type, line);
        }
        return "bool";
    }

    @Override
    public String visitMulDiv(MiniLangParser.MulDivContext ctx) {
        int line = ctx.getStart().getLine();
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));
        checkNumeric(left, line);
        checkNumeric(right, line);

        if (ctx.getChild(1).getText().equals("/")) {
            String rightText = ctx.expr(1).getText();
            if (rightText.equals("0") || rightText.equals("0.0")) {
                throw new SemanticException("División por cero detectada.", line);
            }
        }

        return resolveNumericType(left, right);
    }

    @Override
    public String visitAddSub(MiniLangParser.AddSubContext ctx) {
        int line = ctx.getStart().getLine();
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));

        if (ctx.getChild(1).getText().equals("+") && left.equals("string") && right.equals("string")) {
            return "string";
        }

        checkNumeric(left, line);
        checkNumeric(right, line);
        return resolveNumericType(left, right);
    }

    @Override
    public String visitRelational(MiniLangParser.RelationalContext ctx) {
        int line = ctx.getStart().getLine();
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));
        checkNumeric(left, line);
        checkNumeric(right, line);
        return "bool";
    }

    @Override
    public String visitEquality(MiniLangParser.EqualityContext ctx) {
        int line = ctx.getStart().getLine();
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));
        if (!left.equals(right)) {
            throw new SemanticException(
                "No se puede comparar '" + left + "' con '" + right + "'.", line);
        }
        return "bool";
    }

    @Override
    public String visitAnd(MiniLangParser.AndContext ctx) {
        int line = ctx.getStart().getLine();
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));
        if (!left.equals("bool") || !right.equals("bool")) {
            throw new SemanticException("'&&' requiere operandos bool.", line);
        }
        return "bool";
    }

    @Override
    public String visitOr(MiniLangParser.OrContext ctx) {
        int line = ctx.getStart().getLine();
        String left = visit(ctx.expr(0));
        String right = visit(ctx.expr(1));
        if (!left.equals("bool") || !right.equals("bool")) {
            throw new SemanticException("'||' requiere operandos bool.", line);
        }
        return "bool";
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void checkNumeric(String type, int line) {
        if (!type.equals("int") && !type.equals("float")) {
            throw new SemanticException(
                "Se esperaba tipo numérico (int o float), se encontró: " + type, line);
        }
    }

    private String resolveNumericType(String left, String right) {
        if (left.equals("float") || right.equals("float")) return "float";
        return "int";
    }

    private void checkTypeCompatibility(String expected, String actual, int line) {
        boolean numericCompat = (expected.equals("int") || expected.equals("float"))
                             && (actual.equals("int") || actual.equals("float"));
        if (!expected.equals(actual) && !numericCompat) {
            throw new SemanticException(
                "Tipo incompatible: se esperaba '" + expected + "' pero se encontró '" + actual + "'.", line);
        }
    }
}