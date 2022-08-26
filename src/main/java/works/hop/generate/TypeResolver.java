package works.hop.generate;

import com.squareup.javapoet.TypeName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TypeResolver {

    public static TypeName resolve(String packageName, String type) throws ClassNotFoundException {
        return TypeName.get(resolveClass(packageName, type));
    }

    public static Class<?> resolveClass(String packageName, String type) throws ClassNotFoundException {
        switch (type) {
            case "serial":
            case "int":
            case "INT":
            case "Integer":
            case "INTEGER":
                return Integer.class;
            case "long":
            case "Long":
                return Long.class;
            case "float":
            case "Float":
                return Float.class;
            case "BigDecimal":
                return BigDecimal.class;
            case "String":
            case "text":
            case "TEXT":
                return String.class;
            case "Date":
                return LocalDate.class;
            case "DateTime":
                return LocalDateTime.class;
            default:
                return Class.forName(String.format("%s.%s", packageName, type));
        }
    }
}
