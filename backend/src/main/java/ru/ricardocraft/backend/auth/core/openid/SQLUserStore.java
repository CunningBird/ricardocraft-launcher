package ru.ricardocraft.backend.auth.core.openid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ricardocraft.backend.auth.HikariSQLSourceConfig;
import ru.ricardocraft.backend.auth.core.User;
import ru.ricardocraft.backend.base.ClientPermissions;

import java.sql.SQLException;
import java.util.UUID;

public class SQLUserStore implements UserStore {

    private final Logger logger = LoggerFactory.getLogger(SQLUserStore.class);

    private static final String CREATE_USER_TABLE = """
            create table if not exists `gravit_user` (
              id int auto_increment,
              uuid varchar(36),
              username varchar(255),
              primary key (id),
              unique (uuid),
              unique (username)
            )
            """;
    private static final String INSERT_USER = """
            insert into `gravit_user` (uuid, username) values (?, ?)
            """;
    private static final String DELETE_USER_BY_NAME = """
            delete `gravit_user` where username = ?
            """;
    private static final String SELECT_USER_BY_NAME = """
            select uuid, username from `gravit_user` where username = ?
            """;
    private static final String SELECT_USER_BY_UUID = """
            select uuid, username from `gravit_user` where uuid = ?
            """;

    private final HikariSQLSourceConfig sqlSourceConfig;

    public SQLUserStore(HikariSQLSourceConfig sqlSourceConfig) {
        this.sqlSourceConfig = sqlSourceConfig;
    }

    @Override
    public User getByUsername(String username) {
        try (var connection = sqlSourceConfig.getConnection();
             var selectUserStmt = connection.prepareStatement(SELECT_USER_BY_NAME)) {
            selectUserStmt.setString(1, username);
            try (var rs = selectUserStmt.executeQuery()) {
                if (!rs.next()) {
                    logger.debug("User not found, username: {}", username);
                    return null;
                }
                return new UserEntity(rs.getString("username"),
                        UUID.fromString(rs.getString("uuid")),
                        new ClientPermissions());
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        return null;
    }

    @Override
    public User getUserByUUID(UUID uuid) {
        try (var connection = sqlSourceConfig.getConnection();
             var selectUserStmt = connection.prepareStatement(SELECT_USER_BY_UUID)) {
            selectUserStmt.setString(1, uuid.toString());
            try (var rs = selectUserStmt.executeQuery()) {
                if (!rs.next()) {
                    logger.debug("User not found, UUID: {}", uuid);
                    return null;
                }
                return new UserEntity(rs.getString("username"),
                        UUID.fromString(rs.getString("uuid")),
                        new ClientPermissions());
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        return null;
    }

    @Override
    public void createOrUpdateUser(User user) {
        try (var connection = sqlSourceConfig.getConnection()) {
            connection.setAutoCommit(false);
            var savepoint = connection.setSavepoint();
            try (var deleteUserStmt = connection.prepareStatement(DELETE_USER_BY_NAME);
                 var insertUserStmt = connection.prepareStatement(INSERT_USER)) {
                deleteUserStmt.setString(1, user.getUsername());
                deleteUserStmt.execute();
                insertUserStmt.setString(1, user.getUUID().toString());
                insertUserStmt.setString(2, user.getUsername());
                insertUserStmt.execute();
                connection.commit();
                logger.debug("User saved. UUID: {}, username: {}", user.getUUID(), user.getUsername());
            } catch (Exception e) {
                connection.rollback(savepoint);
                throw e;
            }
        } catch (SQLException e) {
            logger.debug("Failed to save user");
            logger.error(e.getMessage());
            throw new RuntimeException("Failed to save user", e);
        }
    }

    public void init() {
        try (var connection = sqlSourceConfig.getConnection()) {
            connection.setAutoCommit(false);
            var savepoint = connection.setSavepoint();
            try (var createUserTableStmt = connection.prepareStatement(CREATE_USER_TABLE)) {
                createUserTableStmt.execute();
                connection.commit();
            } catch (Exception e) {
                connection.rollback(savepoint);
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
