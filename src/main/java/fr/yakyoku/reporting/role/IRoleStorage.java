package fr.yakyoku.reporting.role;

import java.util.UUID;

import fr.yakyoku.reporting.role.models.Role;

/**
 * TODO : Refactor all storage logic in its own module java.yakyoku.persist itself split into the various storage methods
 *  - Singular module with access to all data, may be used to handle complex requests crossing multiple domains (e.g. reports by user) would need to be done in app module otherwise
 *  - Would allow new services to access any relevant data without duplicating anything (obviously useful for something like user*role storage)
 *  - Probably easier to ensure cohesive database
 */

public interface IRoleStorage {
    public void initializePlayer(UUID id, String name, Role role) throws Exception;
    public void initializePlayer(UUID id, String name, Role role, boolean secondThread) throws Exception;
    public Role getRoleById(UUID id) throws Exception;
    public Role getRoleById(UUID id, boolean secondThread) throws Exception;
    public void setRoleById(UUID id, Role role) throws Exception;
    public void setRoleById(UUID id, Role role, boolean secondThread) throws Exception;
    public UUID getIdByName(String name) throws Exception;
    public UUID getIdByName(String name, boolean secondThread) throws Exception;
    public String getNameById(UUID id) throws Exception;
    public String getNameById(UUID id, boolean secondThread) throws Exception;
}
