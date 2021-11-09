package me.kor3nt.cheatchecker;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class CheatChecker extends JavaPlugin {

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        File file = new File(this.getDataFolder() + "/location.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        CheckCommand Check = new CheckCommand(getConfig(), yaml, file);
        getCommand("checker").setExecutor(Check);
        getServer().getPluginManager().registerEvents(Check, this);


    }
}
