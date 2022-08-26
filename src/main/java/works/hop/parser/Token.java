package works.hop.parser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Token {

    String token;
    String value;
    int lineNum;

    public Token(String token, int lineNum) {
        this.token = token;
        this.lineNum = lineNum;
    }
}
