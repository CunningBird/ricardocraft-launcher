package ru.ricardocraft.backend.auth.core;

import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.PostgreSQLSourceConfig;
import ru.ricardocraft.backend.auth.SQLSourceConfig;

public class PostgresSQLCoreProvider extends AbstractSQLCoreProvider {
    public PostgreSQLSourceConfig postgresSQLHolder;

    @Override
    public SQLSourceConfig getSQLConfig() {
        return postgresSQLHolder;
    }

    @Override
    public void init(LaunchServer server, AuthProviderPair pair) {
        super.init(server, pair);
        logger.warn("Method 'postgresql' deprecated and may be removed in future release. Please use new 'sql' method: https://gravitlauncher.com/auth");
    }
}
