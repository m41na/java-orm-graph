package works.hop.parser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static works.hop.parser.Tokenizer.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Parser {

    Map<String, EntityNode> entities = new LinkedHashMap<>();
    List<Token> tokens;
    int current = 0;
    EntityNode top;
    String packageName;

    public Parser(List<Token> tokens, String packageName) {
        this.tokens = tokens;
        this.packageName = packageName;
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        Tokenizer tk = new Tokenizer("mapping/mappers.ntt");
        tk.tokenize();
        Parser parser = new Parser(tk.tokens, "works.hop.entity");
        parser.parse();
        parser.entities.forEach((k, v) -> System.out.printf("%s -> %s%n", k, v));
    }

    public void parse() {
        while (current < tokens.size()) {
            Token tk = tokens.get(current);
            if (top == null) {
                if (tk.token.equals(LITERAL)) {
                    Token next = peek();
                    if (next.token.equals(OPEN_CURLY)) {
                        entityWithoutAlias();
                    } else {
                        entityWithAlias();
                    }
                }
            } else {
                if (tk.token.equals(LITERAL)) {
                    Token next = peek();
                    if (next.token.equals(SEMI_COLON)) {
                        entityField();
                    }
                    continue;
                }
                if (tk.token.equals(OPEN_PAREN)) {
                    entityField();
                }
            }

            if (tk.token.equals(CLOSE_CURLY)) {
                current++;
                top = null;
            }
        }
    }

    private Token peek() {
        return tokens.get(current + 1);
    }

    public void entityWithAlias() {
        String tableName = tokens.get(current).value;
        expectIdentifier(AS);
        String entityName = expectLiteral();
        expectSymbol(OPEN_CURLY);
        EntityNode entity = new EntityNode(entityName, tableName, packageName, new HashMap<>());
        entities.put(tableName, entity);
        top = entity;
        current++;
    }

    public void entityWithoutAlias() {
        String tableName = tokens.get(current).value;
        expectSymbol(OPEN_CURLY);
        EntityNode entity = new EntityNode(tableName, tableName, packageName, new HashMap<>());
        entities.put(tableName, entity);
        top = entity;
        current++;
    }

    public void entityField() {
        String tableColumn = entityTableColumn();
        Token tk = peek();
        if (tk.token.equals(OPEN_BRACKET)) {
            expectSymbol(OPEN_BRACKET);
            String fieldName = expectLiteral();
            expectSymbol(COMMA);
            String fieldType = entityFieldType();
            expectSymbol(CLOSE_BRACKET);
            FieldNode fieldNode = new FieldNode(fieldName, tableColumn, fieldType);
            top.fields.put(tableColumn, fieldNode);
        } else {
            String fieldType = entityFieldType();
            FieldNode fieldNode = new FieldNode(tableColumn, tableColumn, fieldType);
            top.fields.put(tableColumn, fieldNode);
        }
        current++;
    }

    public String entityTableColumn() {
        Token tk = tokens.get(current);
        if (tk.token.equals(OPEN_PAREN)) {
            String values = "";
            while (!peek().token.equals(CLOSE_PAREN)) {
                current++;

                String name = tokens.get(current).value;
                if (values.length() == 0) {
                    values = name;
                } else {
                    values = String.format("%s,%s", values, name);
                }

                if (!peek().token.equals(CLOSE_PAREN)) {
                    expectSymbol(COMMA);
                }
            }
            expectSymbol(CLOSE_PAREN);
            expectSymbol(SEMI_COLON);
            return values;
        } else {
            String name = tk.value;
            expectSymbol(SEMI_COLON);
            return name;
        }
    }

    public String entityFieldType() {
        return expectLiteral();
    }

    public void expectSymbol(String symbol) {
        Token tk = tokens.get(current + 1);
        if (!tk.token.equals(symbol)) {
            throw new RuntimeException(
                    String.format("Expected %s on line %d but found %s", symbol, tk.lineNum, tk.value));
        }
        current++;
    }

    public String expectLiteral() {
        Token tk = tokens.get(current + 1);
        if (!tk.token.equals(LITERAL)) {
            throw new RuntimeException(
                    String.format("Expected a literal but found %s", tk.token));
        }
        current++;
        return tk.value;
    }

    public void expectIdentifier(String identifier) {
        Token tk = tokens.get(current + 1);
        if (!tk.value.equals(identifier)) {
            throw new RuntimeException(
                    String.format("Expected %s on line %d but found %s", identifier, tk.lineNum, tk.value));
        }
        current++;
    }
}
