package me.desertfox.dgen.utils;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

/**
 * @author DesertFoxHU
 * @version 1.3.4
 *
 * LoadYML feature added by Xzhauloss
 */

public class CustomYml {

	private JavaPlugin p;
	public File file;
	private FileConfiguration config;
	
	public CustomYml(JavaPlugin p) {
		this.p = p;
	}

	public CustomYml createNew(File file) {
		if(!file.exists()) {
			file.getParentFile().mkdirs();
			p.saveResource(file.getName(), false);
		}

		config = new YamlConfiguration();
		config = YamlConfiguration.loadConfiguration(file);

		return this;
	}
	
	public CustomYml createNew(String fileName) {
		if(!fileName.contains(".yml")) {
			fileName += ".yml";
		}

		file = new File(p.getDataFolder() + File.separator + fileName);
		if(!file.exists()) {
			file.getParentFile().mkdirs();
			p.saveResource(fileName, false);
		}
		
		config = new YamlConfiguration();
		config = YamlConfiguration.loadConfiguration(file);
		
		return this;
	}
	
	public CustomYml createNew(String fileName, boolean loadFromJar) {
		if(!fileName.contains(".yml")) {
			fileName += ".yml";
		}
		
		file = new File(p.getDataFolder() + File.separator + fileName);
		if(!file.exists()) {
			if(loadFromJar) {
				file.getParentFile().mkdirs();
				p.saveResource(fileName, false);
			}
			else {
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		config = new YamlConfiguration();
		config = YamlConfiguration.loadConfiguration(file);
		
		return this;
	}
	
	public CustomYml createNew(String directory, String fileName) {
		if(!fileName.contains(".yml")) {
			fileName += ".yml";
		}
		
		File dir = new File(p.getDataFolder() + File.separator + directory);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		
		file = new File(dir.getAbsolutePath() + File.separator + fileName);
		if(!file.exists()) {
			file.getParentFile().mkdirs();
			p.saveResource(directory + "\\" + fileName, false);
		}
		
		config = new YamlConfiguration();
		config = YamlConfiguration.loadConfiguration(file);
		
		return this;
	}
	
	public CustomYml createNew(String directory, String fileName, boolean loadFromJar) {
		if(!fileName.contains(".yml")) {
			fileName += ".yml";
		}
		
		File dir = new File(p.getDataFolder() + File.separator + directory);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		
		file = new File(dir.getAbsolutePath() + File.separator + fileName);
		if(!file.exists()) {
			if(loadFromJar) {
				file.getParentFile().mkdirs();
				p.saveResource(directory + "\\" + fileName, false);
			}
			else {
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		config = new YamlConfiguration();
		config = YamlConfiguration.loadConfiguration(file);
		
		return this;
	}
	
	public FileConfiguration getConfig() {
		return config;
	}
	
	public void save() {
		try {
			config.save(file);
		}
		catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void load() {
		try {
			config.load(file);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

}