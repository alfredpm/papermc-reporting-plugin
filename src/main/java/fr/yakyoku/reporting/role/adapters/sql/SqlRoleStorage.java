package fr.yakyoku.reporting.role.adapters.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

import fr.yakyoku.reporting.role.IRoleStorage;
import fr.yakyoku.reporting.role.models.Role;

public class SqlRoleStorage implements IRoleStorage {
    Connection conn;

    private String getName() {
        return "Role storage [sql]";
    }

    public SqlRoleStorage(Connection connection) {
        conn = connection;
        this.initializeDatabase();
    }

    private boolean initializeDatabase() {
        Logger.getLogger(getName()).warning("Initializing db");
        try {
            PreparedStatement createUsers = conn.prepareStatement("CREATE TABLE IF NOT EXISTS users ("
                +"id CHAR(36) PRIMARY KEY,"
                +"name VARCHAR(255) NOT NULL,"
                +"role INT NOT NULL"
            +");");
            createUsers.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // private boolean seedDatabase() {
    //     try {
    //         // TODO : add params to seed users
    //         initializePlayer(UUID.randomUUID(), "", Role.PLAYER);
    //         return true;
    //     } catch (Exception e) {
    //         // TODO Auto-generated catch block
    //         e.printStackTrace();
    //         return false;
    //     }
    // }

    @Override
    public void initializePlayer(UUID id, String name, Role role) throws Exception {
        Logger.getLogger(getName()).warning("Initializing player");
        PreparedStatement addUser = conn.prepareStatement("INSERT INTO users VALUES (?, ?, ?);");
        addUser.setString(1, id.toString());
        addUser.setString(2, name);
        addUser.setInt(3, role.ordinal());
        addUser.executeUpdate();
    }

    @Override
    public Role getRoleById(UUID id) throws Exception {
        Logger.getLogger(getName()).warning("Getting role by id");
        PreparedStatement getRole = conn.prepareStatement("SELECT role FROM users WHERE "
        +"id = ?"
        +";");
        getRole.setString(1, id.toString());
        ResultSet result = getRole.executeQuery();
        if(result.next()) {
            Role role = Role.values()[result.getInt("role")];
            Logger.getLogger(getName()).warning("Return"+role);
            return role;
        } else {
            throw new Exception("Not found");
        }
    }

    @Override
    public void setRoleById(UUID id, Role role) throws Exception {
        Logger.getLogger(getName()).warning("Setting role by id");
        PreparedStatement updateReport = conn.prepareStatement("UPDATE users "
        +"SET role = ? " 
	    +"WHERE " 
		    +"id = ?"
        +";");
        updateReport.setInt(1, role.ordinal());
        updateReport.setString(2, id.toString());
        updateReport.executeUpdate();
    }

    @Override
    public UUID getIdByName(String name) throws Exception {
        Logger.getLogger(getName()).warning("Getting id by name");
        PreparedStatement getId = conn.prepareStatement("SELECT id FROM users WHERE name = ?;");
        getId.setString(1, name);
        ResultSet result = getId.executeQuery();
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
    public String getNameById(UUID id) throws Exception {
        Logger.getLogger(getName()).warning("Getting name by id");
        PreparedStatement getName = conn.prepareStatement("SELECT name FROM users WHERE id = ?;");
        getName.setString(1, id.toString());
        ResultSet result = getName.executeQuery();
        if(result.next()) {
            String name = result.getString("name");
            Logger.getLogger(getName()).warning("Return"+name);
            return name;
        } else {
            throw new Exception("Not found");
        }
    }
}
