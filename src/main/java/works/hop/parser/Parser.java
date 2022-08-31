package works.hop.parser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import works.hop.parser.bricks.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

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

    List<String> columnTypes = List.of("Integer", "Long", "BigDecimal", "String", "Date", "DateTime");

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
            EntityDefinition ed = entity_definition();
            EntityNode entity = new EntityNode(ed.getEntityName(), ed.getTableName(), packageName, new HashMap<>());
            FieldDefinitions fds = ed.getFieldDefinitions();
            for (SingleFieldDef sfd : fds.getSingleFieldDefinitions()) {
                FieldNode fieldNode = new FieldNode();
                fieldNode.setType(sfd.getPropertyFieldType().getFieldType());
                fieldNode.setColumn(sfd.getColumnName());
                fieldNode.setName(sfd.getPropertyFieldType().getPropertyName());
                entity.getFields().put(fieldNode.column, fieldNode);
            }
            for (MultiFieldDef mfd : fds.getMultiFieldDefinitions()) {
                FieldNode fieldNode = new FieldNode();
                fieldNode.setType(mfd.getPropertyFieldType().getFieldType());
                fieldNode.setColumn(String.join(",", mfd.getColumnNames()));
                fieldNode.setName(mfd.getPropertyFieldType().getPropertyName());
                entity.getFields().put(fieldNode.column, fieldNode);
            }
            entities.put(ed.getTableName(), entity);
        }
    }

    private Token check() {
        return tokens.get(current);
    }

    public EntityDefinition entity_definition() {
        EntityDefinition entityDef = new EntityDefinition();
        String tableName = table_name();
        entityDef.setTableName(tableName);
        if (check().token.equals(IDENTIFIER)) {
            expectIdentifier(AS);
            String entityName = entity_name();
            entityDef.setEntityName(entityName);
        } else {
            entityDef.setEntityName(tableName);
        }
        expectSymbol(OPEN_CURLY);
        FieldDefinitions fieldDefinitions = new FieldDefinitions();
        field_definitions(fieldDefinitions);
        entityDef.setFieldDefinitions(fieldDefinitions);
        expectSymbol(CLOSE_CURLY);
        return entityDef;
    }

    public void field_definitions(FieldDefinitions def) {
        if (check().token.equals(OPEN_PAREN)) {
            MultiFieldDef multiFieldDef = multi_field_definition();
            def.getMultiFieldDefinitions().add(multiFieldDef);
        } else {
            SingleFieldDef singleFieldDef = single_field_definition();
            def.getSingleFieldDefinitions().add(singleFieldDef);
        }
        if (!check().token.equals(CLOSE_CURLY)) {
            field_definitions(def);
        }
    }

    public SingleFieldDef single_field_definition() {
        String name = expectLiteral();
        SingleFieldDef singleFieldDef = new SingleFieldDef();
        singleFieldDef.setColumnName(name);
        expectSymbol(COLON);
        if (check().token.equals(OPEN_BRACKET)) {
            PropertyFieldType type = property_field_type();
            singleFieldDef.setPropertyFieldType(type);
        } else {
            String type = expectLiteral();
            singleFieldDef.setPropertyFieldType(new PropertyFieldType(name, type));
        }
        return singleFieldDef;
    }

    public MultiFieldDef multi_field_definition() {
        expectSymbol(OPEN_PAREN);
        List<String> columnNames = column_names();
        expectSymbol(CLOSE_PAREN);
        expectSymbol(COLON);
        PropertyFieldType propertyFieldType = property_field_type();
        return new MultiFieldDef(columnNames, propertyFieldType);
    }

    public PropertyFieldType property_field_type() {
        expectSymbol(OPEN_BRACKET);
        String propertyName = property_name();
        expectSymbol(COMMA);
        String fieldType = field_type();
        expectSymbol(CLOSE_BRACKET);
        return new PropertyFieldType(propertyName, fieldType);
    }

    public List<String> column_names() {
        List<String> columnNames = new LinkedList<>();
        String columnName = column_name();
        if (check().token.equals(COMMA)) {
            columnNames.add(columnName);
            do {
                expectSymbol(COMMA);
                columnName = column_name();
                columnNames.add(columnName);
            } while (check().token.equals(COMMA));
        }
        return columnNames;
    }

    public String field_type() {
        try {
            return type_name();
        } catch (ParseError e) {
            return entity_name();
        }
    }

    public String column_name() {
        return expectLiteral();
    }

    public String property_name() {
        return expectLiteral();
    }

    public String entity_name() {
        return expectLiteral();
    }

    public String table_name() {
        return expectLiteral();
    }

    public String type_name() {
        String type = tokens.get(current).value;
        expectOneOf(columnTypes);
        return type;
    }

    public void expectSymbol(String symbol) {
        Token tk = tokens.get(current);
        if (!tk.token.equals(symbol)) {
            throw new ParseError(
                    String.format("Expected %s on line %d but found %s", symbol, tk.lineNum, tk.value));
        }
        current++;
    }

    public String expectLiteral() {
        Token tk = tokens.get(current);
        if (!tk.token.equals(LITERAL)) {
            throw new ParseError(
                    String.format("Expected a literal but found %s", tk.token));
        }
        current++;
        return tk.value;
    }

    public void expectIdentifier(String identifier) {
        Token tk = tokens.get(current);
        if (!tk.value.equals(identifier)) {
            throw new RuntimeException(
                    String.format("Expected %s on line %d but found %s", identifier, tk.lineNum, tk.value));
        }
        current++;
    }

    public void expectOneOf(List<String> options) {
        String value = tokens.get(current).value;
        if (!options.contains(value)) {
            throw new ParseError(String.format("Expected '%s' to be one of %s", value, options));
        }
        current++;
    }
}
