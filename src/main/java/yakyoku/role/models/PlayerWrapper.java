package yakyoku.role.models;

import org.bukkit.entity.Player;

public class PlayerWrapper {
    public String name;    
    public Role role;

    public PlayerWrapper(Player player) {
        this.name = player.getName();
        this.role = Role.PLAYER;
    }
    public PlayerWrapper(String name, Role role) {
        this.name = name;
        this.role = role;
    }
}
