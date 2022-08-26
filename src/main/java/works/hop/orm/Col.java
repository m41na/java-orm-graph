package works.hop.orm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Col {

    String name;
    String type;
    int ordinal;
    boolean nullable;
    boolean autoIncr;
}
