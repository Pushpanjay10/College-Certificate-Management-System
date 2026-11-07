package com.college.certgen.models;

public class DatabaseConfig {
    public enum DBType {
        SQLITE("SQLite (Local Embedded)", true),
        MONGODB("MongoDB", false),
        ORACLE("Oracle Database", false),
        FIREBASE("Firebase (Firestore / Realtime)", false),
        MYSQL("MySQL / MariaDB", false),
        POSTGRESQL("PostgreSQL", false);

        private final String displayName;
        private final boolean isEmbedded;

        DBType(String displayName, boolean isEmbedded) {
            this.displayName = displayName;
            this.isEmbedded = isEmbedded;
        }

        public String getDisplayName() { return displayName; }
        public boolean isEmbedded() { return isEmbedded; }
    }

    private DBType type = DBType.SQLITE;
    private String host = "localhost";
    private int port = 3306;
    private String databaseName = "college_cert_db";
    private String username = "root";
    private String password = "";
    private String connectionUri = "mongodb://localhost:27017";
    private String oracleSid = "ORCL";
    private String firebaseProjectId = "";
    private String firebaseApiKey = "";
    private String firebaseDatabaseUrl = "https://my-college-project.firebaseio.com";
    private boolean active = true;
    private String lastStatus = "Not tested";

    public DatabaseConfig() {}

    public DBType getType() { return type; }
    public void setType(DBType type) { this.type = type; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getDatabaseName() { return databaseName; }
    public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConnectionUri() { return connectionUri; }
    public void setConnectionUri(String connectionUri) { this.connectionUri = connectionUri; }

    public String getOracleSid() { return oracleSid; }
    public void setOracleSid(String oracleSid) { this.oracleSid = oracleSid; }

    public String getFirebaseProjectId() { return firebaseProjectId; }
    public void setFirebaseProjectId(String firebaseProjectId) { this.firebaseProjectId = firebaseProjectId; }

    public String getFirebaseApiKey() { return firebaseApiKey; }
    public void setFirebaseApiKey(String firebaseApiKey) { this.firebaseApiKey = firebaseApiKey; }

    public String getFirebaseDatabaseUrl() { return firebaseDatabaseUrl; }
    public void setFirebaseDatabaseUrl(String firebaseDatabaseUrl) { this.firebaseDatabaseUrl = firebaseDatabaseUrl; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getLastStatus() { return lastStatus; }
    public void setLastStatus(String lastStatus) { this.lastStatus = lastStatus; }
}
