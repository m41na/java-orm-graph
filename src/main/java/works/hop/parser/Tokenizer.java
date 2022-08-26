package works.hop.parser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Tokenizer {

    static String OPEN_CURLY = "OPEN_CURLY";
    static String CLOSE_CURLY = "CLOSE_CURLY";
    static String OPEN_BRACKET = "OPEN_BRACKET";
    static String CLOSE_BRACKET = "CLOSE_BRACKET";
    static String OPEN_PAREN = "OPEN_PAREN";
    static String CLOSE_PAREN = "CLOSE_PAREN";
    static String COMMA = "COMMA";
    static String SEMI_COLON = "SEMI_COLON";
    static String LITERAL = "LITERAL";
    static String IDENTIFIER = "IDENTIFIER";
    static String AS = "as";

    Map<String, String> symbols = new HashMap<>();
    List<String> identifiers = new ArrayList<>();
    public List<Token> tokens = new LinkedList<>(); //need to maintain order
    int current = 0;
    String content;
    int lineNum = 1;

    public Tokenizer(String file) throws IOException, URISyntaxException {
        this.content = readAsString(file);
        symbols.put("{", OPEN_CURLY);
        symbols.put("}", CLOSE_CURLY);
        symbols.put("[", OPEN_BRACKET);
        symbols.put("]", CLOSE_BRACKET);
        symbols.put("(", OPEN_PAREN);
        symbols.put(")", CLOSE_PAREN);
        symbols.put(",", COMMA);
        symbols.put(":", SEMI_COLON);

        identifiers.add(AS);
    }

    public static String readAsString(String resourceFile) throws IOException, URISyntaxException {
        Path path = Paths.get(Objects.requireNonNull(Tokenizer.class.getClassLoader()
                .getResource(resourceFile)).toURI());
        try (Stream<String> lines = Files.lines(path)) {
            return lines.collect(Collectors.joining("\n"));
        }
    }

    public void tokenize() {
        while (current < content.length()) {
            char ch = content.charAt(current);
            if(symbols.containsKey(ch + "")){
                symbol(ch);
                continue;
            }
            if(ch == '\n'){
                newline();
                continue;
            }
            if (ch == ' ' ) {
                whitespace();
                continue;
            }
            if (Character.isAlphabetic(ch) || ch == '_') {
                word();
            }
        }
    }

    public void symbol(char ch){
        tokens.add(new Token(symbols.get(ch + ""), lineNum));
        current++;
    }

    public void newline(){
        lineNum++;
        current++;
    }

    public void whitespace() {
        char ch = content.charAt(current);
        while (ch == ' ') {
            current++;
            ch = content.charAt(current);
        }
    }

    public void word() {
        int start = current;
        char ch = content.charAt(current);
        while (Character.isAlphabetic(ch) || ch == '_') {
            current++;
            ch = content.charAt(current);
        }

        String value = content.substring(start, current);
        if (identifiers.contains(value)) {
            tokens.add(new Token(IDENTIFIER, value, lineNum));
        } else {
            tokens.add(new Token(LITERAL, value, lineNum));
        }
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        Tokenizer tk = new Tokenizer("mapping/mappers.ntt");
        tk.tokenize();
        tk.tokens.forEach(System.out::println);
    }
}
