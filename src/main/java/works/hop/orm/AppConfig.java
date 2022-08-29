package works.hop.orm;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppConfig {

    static AppConfig appConfig;
    static HikariConfig hkConfig = new HikariConfig();
    static HikariDataSource ds;
    DB db;

    public static AppConfig load(String classpathResource) throws IOException {
        Yaml yaml = new Yaml(new Constructor(AppConfig.class));
        if (appConfig == null) {
            synchronized (AppConfig.class) {
                try (InputStream inputStream = AppConfig.class
                        .getClassLoader()
                        .getResourceAsStream(classpathResource)) {
                    appConfig = yaml.load(inputStream);
                    //configure connection pool
                    hkConfig.setJdbcUrl(appConfig.db.url);
                    hkConfig.setUsername(appConfig.db.username);
                    hkConfig.setPassword(appConfig.db.password);
                    hkConfig.addDataSourceProperty("cachePrepStmts", "true");
                    hkConfig.addDataSourceProperty("prepStmtCacheSize", "250");
                    hkConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                    ds = new HikariDataSource(hkConfig);
                }
            }
        }
        return appConfig;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DB {
        String url;
        String username;
        String password;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(AppConfig.load("app.yaml"));
    }
}
