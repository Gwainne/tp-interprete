import java.util.*;

public class SymbolTable {

    private final Deque<Map<String, Symbol>> scopes = new ArrayDeque<>();

    public SymbolTable() {
        pushScope(); // scope global
    }

    public void pushScope() {
        scopes.push(new HashMap<>());
    }

    public void popScope() {
        scopes.pop();
    }

    public void declare(String name, String type, int line) {
        Map<String, Symbol> current = scopes.peek();
        if (current.containsKey(name)) {
            throw new SemanticException("Variable '" + name + "' ya fue declarada.", line);
        }
        current.put(name, new Symbol(name, type));
    }

    public Symbol get(String name, int line) {
        for (Map<String, Symbol> scope : scopes) {
            if (scope.containsKey(name)) return scope.get(name);
        }
        throw new SemanticException("Variable '" + name + "' no fue declarada.", line);
    }

    public boolean exists(String name) {
        for (Map<String, Symbol> scope : scopes) {
            if (scope.containsKey(name)) return true;
        }
        return false;
    }

    public static class Symbol {
        public final String name;
        public final String type;

        public Symbol(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }
}