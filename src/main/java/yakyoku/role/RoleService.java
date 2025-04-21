package yakyoku.role;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import yakyoku.role.models.Role;

public class RoleService {
    
    private String getName() {
        return "Role Service";
    }

    private IRoleStorage storage;
    
    public RoleService(IRoleStorage storage) {
        this.storage = storage;
    }

    public Optional<UUID> getIdByName(String playerName) {
        try {
            return Optional.of(this.storage.getIdByName(playerName));
        } 
        catch (Exception e) {
            Logger.getLogger(getName()).warning(e.getMessage());
            return Optional.empty();
        }
    }
    public String getNameById(UUID id) {
        try {
            return this.storage.getNameById(id);
        } 
        catch (Exception e) {
            Logger.getLogger(getName()).warning(e.getMessage());
            return null;
        }
    }
    public Optional<Role> getPlayerRole(UUID playerId) {
        try {
            return Optional.of(this.storage.getRoleById(playerId));
        } 
        catch (Exception e) {
            Logger.getLogger(getName()).warning(e.getMessage());
            return Optional.empty();
        }
    }
    // public boolean addPlayerRole(UUID playerId, Role role) {
    //     try {
    //         this.storage.setRoleById(playerId, role);
    //         //Actually change permissions
    //         Player player = Bukkit.getPlayer(playerId);
    //         if(player != null) {
                
    //         }
    //         return true;
    //     } 
    //     catch (Exception e) {
    //         Logger.getLogger(getName()).severe(e.getMessage());
    //         return false;
    //     }
    // }
    // public boolean addPlayer(UUID playerId, String playerName, Role role) {
    //     try {
    //         this.storage.initializePlayer(playerId, playerName, role);
    //         return true;
    //     } 
    //     catch (Exception e) {
    //         Logger.getLogger(getName()).severe(e.getMessage());
    //         return false;
    //     }
    // }
    public Role getOrCreatePlayer(UUID playerId, String playerName, Role defaultRole) {
        try {
            return this.storage.getRoleById(playerId);
        } catch (Exception e) {

        }
        try {
            this.storage.initializePlayer(playerId, playerName, defaultRole);
            return defaultRole;
        } catch (Exception e1) {
            return Role.NONE;
        }
    }
}
