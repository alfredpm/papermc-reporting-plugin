package yakyoku.role.adapters;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import yakyoku.role.IRoleStorage;
import yakyoku.role.models.PlayerWrapper;
import yakyoku.role.models.Role;

public class InMemoryRoleStorage implements IRoleStorage {
    Map<UUID, PlayerWrapper> storage = new HashMap<>();

    private String getName() {
        return "Role Storage [In Memory]";
    }

    @Override
    public void initializePlayer(UUID id, String name, Role role) throws Exception {
        try {
            storage.put(id, new PlayerWrapper(name, role));
        } 
        catch (Exception e) {
            throw new IllegalArgumentException("Something is wrong with the creation of a role for this user.");
        }
    }

    @Override
    public Role getRoleById(UUID id) throws Exception {
        try {
            return storage.get(id).role;
        } 
        catch (Exception e) {
            throw new NoSuchElementException("No player found with id :"+id);
        }
    }

    @Override
    public void setRoleById(UUID id, Role role) {       
        try {
            PlayerWrapper player = storage.get(id);
            player.role = role;
        } 
        catch (Exception e) {
            throw new NoSuchElementException("No player found with id :"+id);
        }
    }

    @Override
    public UUID getIdByName(String name) throws Exception {
        Optional<Entry<UUID, PlayerWrapper>> foundEntry = storage.entrySet().stream().filter(entry -> entry.getValue().name.equals(name)).findFirst();
        try {
            return foundEntry.get().getKey();
        }
        catch (Exception e){
            throw new NoSuchElementException("No player found for name : "+name);
        }
    }

    @Override
    public String getNameById(UUID id) throws Exception {
        try {
            return storage.get(id).name;
        }
        catch (Exception e){
            throw new NoSuchElementException("No player found with id :"+id);
        }
    }

    public void logStorage() {
        String message = "Memory :";
        Logger.getLogger(getName()).info(message);
        storage.entrySet().stream().forEach((entry) -> {
            Logger.getLogger(getName()).info(String.format("%s: %s / %s", entry.getKey(), entry.getValue().name, entry.getValue().role));
        });
    }
}
