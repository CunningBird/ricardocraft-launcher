package ru.ricardocraft.bff.auth.core;

import ru.ricardocraft.bff.LaunchServer;
import ru.ricardocraft.bff.auth.AuthProviderPair;
import ru.ricardocraft.bff.auth.PostgreSQLSourceConfig;
import ru.ricardocraft.bff.auth.SQLSourceConfig;

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
