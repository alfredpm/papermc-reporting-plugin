package fr.yakyoku.reporting.role.adapters.memory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.commons.lang.NotImplementedException;

import fr.yakyoku.reporting.role.IRoleStorage;
import fr.yakyoku.reporting.role.models.PlayerWrapper;
import fr.yakyoku.reporting.role.models.Role;

public class InMemoryRoleStorage implements IRoleStorage {
    Map<UUID, PlayerWrapper> storage = new HashMap<>();

    private String getName() {
        return "Role Storage [In Memory]";
    }

    @Override
    public void initializePlayer(UUID id, String name, Role role) throws Exception { initializePlayer(id, name, role, false); }
    @Override
    public void initializePlayer(UUID id, String name, Role role, boolean secondThread) throws Exception {
        if(secondThread) {
            throw new NotImplementedException(getName()+" does not support multithread for initializePlayer.");
        }
        try {
            storage.put(id, new PlayerWrapper(name, role));
        } 
        catch (Exception e) {
            throw new IllegalArgumentException("Something is wrong with the creation of a role for this user.");
        }
    }

    @Override
    public Role getRoleById(UUID id) throws Exception { return getRoleById(id, false); }
    @Override
    public Role getRoleById(UUID id, boolean secondThread) throws Exception {
        if(secondThread) {
            throw new NotImplementedException(getName()+" does not support multithread for getRoleById.");
        }
        try {
            return storage.get(id).role;
        } 
        catch (Exception e) {
            throw new NoSuchElementException("No player found with id :"+id);
        }
    }

    @Override
    public void setRoleById(UUID id, Role role) throws Exception { setRoleById(id, role, false); }
    @Override
    public void setRoleById(UUID id, Role role, boolean secondThread) throws Exception {
        if(secondThread) {
            throw new NotImplementedException(getName()+" does not support multithread for setRoleById.");
        }      
        try {
            PlayerWrapper player = storage.get(id);
            player.role = role;
        } 
        catch (Exception e) {
            throw new NoSuchElementException("No player found with id :"+id);
        }
    }

    @Override
    public UUID getIdByName(String name) throws Exception { return getIdByName(name, false); }
    @Override
    public UUID getIdByName(String name, boolean secondThread) throws Exception {
        if(secondThread) {
            throw new NotImplementedException(getName()+" does not support multithread for getIdByName.");
        }
        Optional<Entry<UUID, PlayerWrapper>> foundEntry = storage.entrySet().stream().filter(entry -> entry.getValue().name.equals(name)).findFirst();
        try {
            return foundEntry.get().getKey();
        }
        catch (Exception e){
            throw new NoSuchElementException("No player found for name : "+name);
        }
    }

    @Override
    public String getNameById(UUID id) throws Exception { return getNameById(id, false); }
    @Override
    public String getNameById(UUID id, boolean secondThread) throws Exception {
        if(secondThread) {
            throw new NotImplementedException(getName()+" does not support multithread for getNameById.");
        }
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
