public class Interpreter extends MiniLangBaseVisitor<Object> {

    private final SymbolTable symbolTable = new SymbolTable();
    private final java.util.Map<String, Object> memory = new java.util.HashMap<>();

    // ── Block ─────────────────────────────────────────────────────────────────

    @Override
    public Object visitBlock(MiniLangParser.BlockContext ctx) {
        symbolTable.pushScope();
        for (MiniLangParser.StatementContext stmt : ctx.statement()) {
            visit(stmt);
        }
        symbolTable.popScope();
        return null;
    }

    // ── Declaración ──────────────────────────────────────────────────────────

    @Override
    public Object visitDeclaration(MiniLangParser.DeclarationContext ctx) {
        String type = ctx.type().getText();
        String name = ctx.ID().getText();
        int line = ctx.getStart().getLine();

        Object value = visit(ctx.expr());
        value = coerce(type, value);

        symbolTable.declare(name, type, line);
        memory.put(name, value);

        return null;
    }

    // ── Asignación ───────────────────────────────────────────────────────────

    @Override
    public Object visitAssignment(MiniLangParser.AssignmentContext ctx) {
        String name = ctx.ID().getText();
        int line = ctx.getStart().getLine();

        SymbolTable.Symbol symbol = symbolTable.get(name, line);
        Object value = visit(ctx.expr());
        value = coerce(symbol.type, value);

        memory.put(name, value);
        return null;
    }

    // ── Print ─────────────────────────────────────────────────────────────────

    @Override
    public Object visitPrintStmt(MiniLangParser.PrintStmtContext ctx) {
        Object value = visit(ctx.expr());
        System.out.println(value);
        return null;
    }

    // ── If ───────────────────────────────────────────────────────────────────

    @Override
    public Object visitIfStmt(MiniLangParser.IfStmtContext ctx) {
        boolean condition = (Boolean) visit(ctx.expr());

        if (condition) {
            visit(ctx.block(0));
        } else if (ctx.block().size() > 1) {
            visit(ctx.block(1));
        }

        return null;
    }

    // ── Repeat-Until ─────────────────────────────────────────────────────────

    @Override
    public Object visitRepeatStmt(MiniLangParser.RepeatStmtContext ctx) {
        do {
            visit(ctx.block());
        } while (!(Boolean) visit(ctx.expr()));

        return null;
    }

    // ── Expresiones ───────────────────────────────────────────────────────────

    @Override
    public Object visitIntLit(MiniLangParser.IntLitContext ctx) {
        return Integer.parseInt(ctx.INT().getText());
    }

    @Override
    public Object visitFloatLit(MiniLangParser.FloatLitContext ctx) {
        return Double.parseDouble(ctx.FLOAT().getText());
    }

    @Override
    public Object visitStringLit(MiniLangParser.StringLitContext ctx) {
        String raw = ctx.STRING().getText();
        return raw.substring(1, raw.length() - 1);
    }

    @Override
    public Object visitBoolLit(MiniLangParser.BoolLitContext ctx) {
        return Boolean.parseBoolean(ctx.BOOL().getText());
    }

    @Override
    public Object visitVar(MiniLangParser.VarContext ctx) {
        String name = ctx.ID().getText();
        return memory.get(name);
    }

    @Override
    public Object visitParens(MiniLangParser.ParensContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Object visitNot(MiniLangParser.NotContext ctx) {
        return !(Boolean) visit(ctx.expr());
    }

    @Override
    public Object visitMulDiv(MiniLangParser.MulDivContext ctx) {
        Object left = visit(ctx.expr(0));
        Object right = visit(ctx.expr(1));
        String op = ctx.getChild(1).getText();

        if (op.equals("/")) {
            double divisor = toDouble(right);
            if (divisor == 0) {
                throw new SemanticException("División por cero en tiempo de ejecución.",
                    ctx.getStart().getLine());
            }
        }

        if (left instanceof Double || right instanceof Double) {
            double l = toDouble(left), r = toDouble(right);
            return op.equals("*") ? l * r : l / r;
        } else {
            int l = (Integer) left, r = (Integer) right;
            return op.equals("*") ? l * r : l / r;
        }
    }

    @Override
    public Object visitAddSub(MiniLangParser.AddSubContext ctx) {
        Object left = visit(ctx.expr(0));
        Object right = visit(ctx.expr(1));
        String op = ctx.getChild(1).getText();

        if (left instanceof String && right instanceof String && op.equals("+")) {
            return (String) left + (String) right;
        }

        if (left instanceof Double || right instanceof Double) {
            double l = toDouble(left), r = toDouble(right);
            return op.equals("+") ? l + r : l - r;
        } else {
            int l = (Integer) left, r = (Integer) right;
            return op.equals("+") ? l + r : l - r;
        }
    }

    @Override
    public Object visitRelational(MiniLangParser.RelationalContext ctx) {
        Object left = visit(ctx.expr(0));
        Object right = visit(ctx.expr(1));
        String op = ctx.getChild(1).getText();
        double l = toDouble(left), r = toDouble(right);

        return switch (op) {
            case "<"  -> l < r;
            case ">"  -> l > r;
            case "<=" -> l <= r;
            case ">=" -> l >= r;
            default   -> false;
        };
    }

    @Override
    public Object visitEquality(MiniLangParser.EqualityContext ctx) {
        Object left = visit(ctx.expr(0));
        Object right = visit(ctx.expr(1));
        String op = ctx.getChild(1).getText();
        boolean eq = left.equals(right);
        return op.equals("==") ? eq : !eq;
    }

    @Override
    public Object visitAnd(MiniLangParser.AndContext ctx) {
        return (Boolean) visit(ctx.expr(0)) && (Boolean) visit(ctx.expr(1));
    }

    @Override
    public Object visitOr(MiniLangParser.OrContext ctx) {
        return (Boolean) visit(ctx.expr(0)) || (Boolean) visit(ctx.expr(1));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private double toDouble(Object val) {
        if (val instanceof Double) return (Double) val;
        if (val instanceof Integer) return ((Integer) val).doubleValue();
        throw new RuntimeException("Se esperaba número, se encontró: " + val);
    }

    private Object coerce(String type, Object value) {
        if (type.equals("float") && value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }
        return value;
    }
}