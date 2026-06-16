import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

        if (args.length < 1) {
            System.err.println("Uso: java Main <archivo.ml>");
            System.exit(1);
        }

        // 1. Leer el archivo fuente
        CharStream input = CharStreams.fromFileName(args[0]);

        // 2. Análisis léxico
        MiniLangLexer lexer = new MiniLangLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> rec, Object sym,
                                    int line, int col, String msg, RecognitionException e) {
                System.err.println("[Error léxico - línea " + line + ":" + col + "] " + msg);
                System.exit(1);
            }
        });

        // 3. Análisis sintáctico
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniLangParser parser = new MiniLangParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> rec, Object sym,
                                    int line, int col, String msg, RecognitionException e) {
                System.err.println("[Error sintáctico - línea " + line + ":" + col + "] " + msg);
                System.exit(1);
            }
        });

        ParseTree tree = parser.program();

        // 4. Análisis semántico
        try {
            new SemanticAnalyzer().visit(tree);
        } catch (SemanticException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        // 5. Ejecución
        try {
            new Interpreter().visit(tree);
        } catch (SemanticException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("[Error en tiempo de ejecución] " + e.getMessage());
            System.exit(1);
        }
    }
}