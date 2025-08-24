package me.confuser.banmanager.bukkit.tigerreports;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author MrTigreroux
 * Adapted for BanManager TigerReports integration
 */
public enum ConfigSound {
    
    MENU("ITEM_PICKUP", "ENTITY_ITEM_PICKUP"),
    ERROR("ITEM_BREAK", "ENTITY_ITEM_BREAK"),
    REPORT("BAT_DEATH", "ENTITY_BAT_DEATH"),
    STAFF("ITEM_PICKUP", "ENTITY_ITEM_PICKUP"),
    TELEPORT("ENDERMAN_TELEPORT", "ENTITY_ENDERMEN_TELEPORT");
    
    private final String oldSound;
    private final String newSound;
    
    ConfigSound(String oldSound, String newSound) {
        this.oldSound = oldSound;
        this.newSound = newSound;
    }
    
    public String getConfigName() {
        String name = name();
        return name.charAt(0) + name.substring(1).toLowerCase() + "Sound";
    }
    
    public Sound get(FileConfiguration config) {
        String path = "Config." + getConfigName();
        String configSound = config.getString(path);
        if (configSound != null && !configSound.equalsIgnoreCase("none")) {
            for (String sound : new String[] {
                    configSound.toUpperCase(), oldSound, newSound
            }) {
                try {
                    return Sound.valueOf(sound);
                } catch (Exception invalidSound) {}
            }
        }
        return null;
    }
    
    public void play(Player p, FileConfiguration config) {
        Sound sound = get(config);
        if (sound != null) {
            p.playSound(p.getLocation(), sound, 1, 1);
        }
    }
    
}

