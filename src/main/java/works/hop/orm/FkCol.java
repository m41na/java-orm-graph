package works.hop.orm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FkCol {

    String fkColumn;
    String fkTable;
    String fkName;
    String pkColumn;
    String pkTable;
}
