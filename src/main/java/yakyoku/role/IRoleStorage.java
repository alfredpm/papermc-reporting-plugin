package yakyoku.role;

import java.util.UUID;

import yakyoku.role.models.Role;

public interface IRoleStorage {
    public void initializePlayer(UUID id, String name, Role role) throws Exception;
    public Role getRoleById(UUID id) throws Exception;
    public void setRoleById(UUID id, Role role) throws Exception;
    public UUID getIdByName(String name) throws Exception;
    public String getNameById(UUID id) throws Exception;
}
