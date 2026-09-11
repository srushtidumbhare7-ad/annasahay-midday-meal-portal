package com.annasahay.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${spring.datasource.url:}")
    private String rawUrl;

    @Value("${spring.datasource.username:}")
    private String rawUsername;

    @Value("${spring.datasource.password:}")
    private String rawPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        String dbUrl = rawUrl;
        if (dbUrl == null || dbUrl.isBlank()) {
            dbUrl = System.getenv("SPRING_DATASOURCE_URL");
        }
        if (dbUrl == null || dbUrl.isBlank()) {
            dbUrl = System.getenv("DATABASE_URL");
        }
        if (dbUrl == null || dbUrl.isBlank()) {
            dbUrl = System.getenv("MYSQL_URL");
        }

        String username = rawUsername;
        if (username == null || username.isBlank()) {
            username = System.getenv("SPRING_DATASOURCE_USERNAME");
        }

        String password = rawPassword;
        if (password == null || password.isBlank()) {
            password = System.getenv("SPRING_DATASOURCE_PASSWORD");
        }

        // Clean up quotes if accidentally entered in Render UI
        if (dbUrl != null) {
            dbUrl = dbUrl.trim().replaceAll("^\"|\"$|^'|'$", "");
        }
        if (username != null) {
            username = username.trim().replaceAll("^\"|\"$|^'|'$", "");
        }
        if (password != null) {
            password = password.trim().replaceAll("^\"|\"$|^'|'$", "");
        }

        // Check individual Railway MySQL environment variables (MYSQLHOST, MYSQLPORT, MYSQLDATABASE)
        String railwayHost = System.getenv("MYSQLHOST");
        if ((dbUrl == null || dbUrl.isBlank()) && railwayHost != null && !railwayHost.isBlank()) {
            String railwayPort = System.getenv("MYSQLPORT");
            if (railwayPort == null || railwayPort.isBlank()) railwayPort = "3306";
            String railwayDb = System.getenv("MYSQLDATABASE");
            if (railwayDb == null || railwayDb.isBlank()) railwayDb = "railway";
            dbUrl = "jdbc:mysql://" + railwayHost + ":" + railwayPort + "/" + railwayDb + "?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
            
            String rUser = System.getenv("MYSQLUSER");
            if (rUser != null && !rUser.isBlank()) {
                username = rUser;
            }
            String rPass = System.getenv("MYSQLPASSWORD");
            if (rPass != null && !rPass.isBlank()) {
                password = rPass;
            }
            log.info("Auto-configured DataSource from Railway environment variables: {}:{}", railwayHost, railwayPort);
        }

        // Auto-convert raw Aiven / Railway URI (mysql://user:pass@host:port/dbname) to valid JDBC URL
        if (dbUrl != null && dbUrl.startsWith("mysql://")) {
            try {
                URI uri = new URI(dbUrl);
                String host = uri.getHost();
                int port = uri.getPort() > 0 ? uri.getPort() : 3306;
                String path = uri.getPath();
                String dbName = (path != null && path.length() > 1) ? path.substring(1) : "railway";

                if (uri.getUserInfo() != null) {
                    String[] userInfo = uri.getUserInfo().split(":", 2);
                    if (userInfo.length > 0 && (username == null || username.isBlank())) {
                        username = userInfo[0];
                    }
                    if (userInfo.length > 1 && (password == null || password.isBlank())) {
                        password = userInfo[1];
                    }
                }

                // Internal cloud networks (like Railway private network or localhost) do not use SSL
                String sslParam = "sslMode=PREFERRED";
                if (host != null && (host.contains("railway.internal") || host.contains("localhost") || host.contains("127.0.0.1"))) {
                    sslParam = "useSSL=false";
                }

                dbUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?" + sslParam + "&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                log.info("Converted mysql:// to JDBC format for host: {}:{}", host, port);
            } catch (Exception e) {
                log.warn("Could not parse mysql:// URI as RFC 2396, prepending jdbc:: {}", e.getMessage());
                dbUrl = "jdbc:" + dbUrl;
            }
        }

        // Ensure sslMode is camelCase for MySQL Connector/J
        if (dbUrl != null && dbUrl.contains("ssl-mode=")) {
            dbUrl = dbUrl.replace("ssl-mode=", "sslMode=");
        }

        // Test network reachability of the configured database (localhost, Aiven, Railway, etc.)
        boolean dbReachable = false;
        String testHost = "localhost";
        int testPort = 3306;

        if (dbUrl != null && dbUrl.startsWith("jdbc:mysql://")) {
            try {
                String clean = dbUrl.substring("jdbc:mysql://".length());
                int slashIdx = clean.indexOf('/');
                String hostPort = (slashIdx > 0) ? clean.substring(0, slashIdx) : clean;
                int qIdx = hostPort.indexOf('?');
                if (qIdx > 0) {
                    hostPort = hostPort.substring(0, qIdx);
                }

                if (hostPort.contains(":")) {
                    String[] parts = hostPort.split(":", 2);
                    testHost = parts[0].trim();
                    testPort = Integer.parseInt(parts[1].trim());
                } else {
                    testHost = hostPort.trim();
                    testPort = 3306;
                }
                log.info("Testing database reachability at {}:{} (timeout: 2500ms)...", testHost, testPort);
                dbReachable = isPortReachable(testHost, testPort, 2500);
            } catch (Exception e) {
                log.warn("Could not parse host/port from dbUrl to test reachability: {}", e.getMessage());
                dbReachable = false;
            }
        } else if (dbUrl != null && !dbUrl.isBlank()) {
            dbReachable = true;
        }

        boolean useH2 = false;
        if (!dbReachable) {
            useH2 = true;
            log.warn("===============================================================================");
            log.warn(">> Target database at {}:{} is UNREACHABLE or DOWN!", testHost, testPort);
            log.warn(">> Auto-switching to high-performance in-memory H2 database (MySQL compatibility mode)");
            log.warn(">> Cloud deployment will boot up in seconds with pre-seeded demo data!");
            log.warn("===============================================================================");
        }

        String driverClass;
        if (useH2) {
            dbUrl = "jdbc:h2:mem:annasahay;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=DAY";
            username = "sa";
            password = "";
            driverClass = "org.h2.Driver";
        } else {
            if (dbUrl == null || dbUrl.isBlank()) {
                dbUrl = "jdbc:mysql://localhost:3306/annasahay?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            }
            if (username == null || username.isBlank()) {
                username = "root";
            }
            if (password == null) {
                password = "";
            }
            driverClass = "com.mysql.cj.jdbc.Driver";
        }

        log.info("Configuring DataSource with URL: {}", dbUrl);
        log.info("Configuring DataSource with Username: {}", username);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClass);
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(900000);
        config.setConnectionTimeout(10000);
        config.setInitializationFailTimeout(10000);

        return new HikariDataSource(config);
    }

    private boolean isPortReachable(String host, int port, int timeoutMs) {
        try (java.net.Socket socket = new java.net.Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
