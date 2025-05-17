package fr.yakyoku.reporting.role.adapters.dbapi;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang.NotImplementedException;

import fr.naruse.dbapi.api.DatabaseAPI;
import fr.naruse.dbapi.database.Database;
import fr.naruse.dbapi.sql.SQLHelper;
import fr.naruse.dbapi.sql.SQLRequest;
import fr.naruse.dbapi.sql.SQLResponse;
import fr.yakyoku.reporting.role.IRoleStorage;
import fr.yakyoku.reporting.role.models.Role;

public class DbApiRoleStorage implements IRoleStorage {

    private final String TABLE_ROLE_NAME = "users";
    private final Database roleDatabase;

    private String getName() {
        return "Role storage [dbapi]";
    }

    public DbApiRoleStorage() {
        DatabaseAPI.createNewDatabase(this.roleDatabase = new Database("Users", TABLE_ROLE_NAME) {
            @Override
            public String getQuery() {
                return "CREATE TABLE IF NOT EXISTS " + TABLE_ROLE_NAME + " ("
                    +"id CHAR(36) PRIMARY KEY,"
                    +"name VARCHAR(255) NOT NULL,"
                    +"role INT NOT NULL"
                +");";
            }
        });
    }

    @Override
    public void initializePlayer(UUID id, String name, Role role) throws Exception { initializePlayer(id, name, role, false); }
    @Override
    public void initializePlayer(UUID id, String name, Role role, boolean secondThread) throws Exception {
        SQLRequest sqlRequest = new SQLRequest(SQLHelper.getInsertRequest(TABLE_ROLE_NAME, new String[]{"id", "name", "role"}),
                id.toString(),
                name,
                role.ordinal());
        if (secondThread) {
            this.roleDatabase.prepareStatement(sqlRequest);
        } else {
            this.roleDatabase.prepareDirectStatement(sqlRequest);
        }
    }

    @Override
    public Role getRoleById(UUID id) throws Exception { return getRoleById(id, false); }
    @Override
    public Role getRoleById(UUID id, boolean secondThread) throws Exception {
        SQLRequest sqlRequest = new SQLRequest(SQLHelper.getSelectRequest(TABLE_ROLE_NAME, new String[]{"name", "role"}, "id"),
            id.toString()
        );
        // if (secondThread) {
        //     this.roleDatabase.prepareStatement(sqlRequest, response);
        // } else {
        //     this.roleDatabase.prepareDirectStatement(sqlRequest, response);
        // }
        ResultSet result = this.roleDatabase.getResultSet(sqlRequest);
        if(result.next()) {
            Role role = Role.values()[result.getInt("role")];
            Logger.getLogger(getName()).warning("Return"+role);
            return role;
        } else {
            throw new Exception("Not found");
        }
    }

    @Override
    public void setRoleById(UUID id, Role role) throws Exception { setRoleById(id, role, false); }
    @Override
    public void setRoleById(UUID id, Role role, boolean secondThread) throws Exception {
        SQLRequest sqlRequest = new SQLRequest(SQLHelper.getUpdateRequest(TABLE_ROLE_NAME, new String[]{"role"}, "id"),
                role.ordinal(),
                id.toString());
        if (secondThread) {
            this.roleDatabase.prepareStatement(sqlRequest);
        } else {
            this.roleDatabase.prepareDirectStatement(sqlRequest);
        }
    }

    @Override
    public UUID getIdByName(String name) throws Exception { return getIdByName(name, false); }
    @Override
    public UUID getIdByName(String name, boolean secondThread) throws Exception {
        SQLRequest sqlRequest = new SQLRequest(SQLHelper.getSelectRequest(TABLE_ROLE_NAME, new String[]{"id"}, "name"),
            name
        );
        // if (secondThread) {
        //     this.roleDatabase.prepareStatement(sqlRequest, response);
        // } else {
        //     this.roleDatabase.prepareDirectStatement(sqlRequest, response);
        // }
        ResultSet result = this.roleDatabase.getResultSet(sqlRequest);
        if(result.next()) {
            String strId = result.getString("id");
            UUID res = UUID.fromString(strId);
            Logger.getLogger(getName()).warning("Return"+res);
            return res;
        } else {
            throw new Exception("Not found");
        }
    }

    @Override
    public String getNameById(UUID id) throws Exception { return getNameById(id, false); }
    @Override
    public String getNameById(UUID id, boolean secondThread) throws Exception {
        SQLRequest sqlRequest = new SQLRequest(SQLHelper.getSelectRequest(TABLE_ROLE_NAME, new String[]{"name"}, "id"),
            id.toString()
        );
        // if (secondThread) {
        //     this.roleDatabase.prepareStatement(sqlRequest, response);
        // } else {
        //     this.roleDatabase.prepareDirectStatement(sqlRequest, response);
        // }
        ResultSet result = this.roleDatabase.getResultSet(sqlRequest);
        if(result.next()) {
            String name = result.getString("name");
            Logger.getLogger(getName()).warning("Return"+name);
            return name;
        } else {
            throw new Exception("Not found");
        }
    }
}
