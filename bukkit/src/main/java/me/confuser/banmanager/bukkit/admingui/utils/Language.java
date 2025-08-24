package me.confuser.banmanager.bukkit.admingui.utils;

import me.confuser.banmanager.bukkit.admingui.AdminGuiIntegration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class Language {

	public static ArrayList<String> default_languages = new ArrayList<>(Arrays.asList("Bulgarian", "Chinese", "Czech", "Dutch", "English", "Finnish", "French", "German", "Hebrew", "Italian", "Japanese", "Korean", "Latvian", "Portuguese", "Russian", "Slovak", "Spanish", "Swedish", "Turkish", "Ukrainian"));
	public static ArrayList<String> enabled_languages = new ArrayList<>();
	static HashMap<String, YamlConfiguration> languages = new HashMap<>();

	public static ArrayList<String> getLanguages() {
		enabled_languages.clear();
		File dir = new File(AdminGuiIntegration.getInstance().getDataFolder(), "Languages");
		if (dir.exists()) {
			String[] files = dir.list();
			for (String file : files) {
				if (isLangFile(file)) enabled_languages.add(file.replace(".yml", ""));
			}
		}
		return enabled_languages;
	}

	public static String getMessages(UUID uuid, String config) {
		// Get language with proper null checks and fallbacks
		String configDefaultLang = AdminGuiIntegration.getInstance().getConf().getString("default_language");
		String playerLang = Settings.language.getOrDefault(uuid, configDefaultLang);
		
		// Ensure we always have a language, fallback to English if everything is null
		String language = (playerLang != null) ? playerLang : 
		                 (configDefaultLang != null) ? configDefaultLang : "English";
		
		// Try to get message from the determined language
		if (enabled_languages.contains(language)) {
			YamlConfiguration langConfig = languages.getOrDefault(language, null);
			if (langConfig != null) {
				String result = langConfig.getString(config, null);
				if (result != null) {
					return result;
				}
			}
		}
		
		// Try fallback to English if current language fails
		if (!language.equals("English") && enabled_languages.contains("English")) {
			YamlConfiguration englishConfig = languages.getOrDefault("English", null);
			if (englishConfig != null) {
				String result = englishConfig.getString(config, null);
				if (result != null) {
					return result;
				}
			}
		}
		
		// Only log warnings for missing messages (not debug spam)
		AdminGuiIntegration.getInstance().getLogger().warning("Message '" + config + "' not found in any language!");
		return null;
	}

	public static boolean downloadLanguage(String language) {
		if (default_languages.contains(language)) {
			// Create Languages directory if it doesn't exist
			File languagesDir = new File(AdminGuiIntegration.getInstance().getDataFolder(), "Languages");
			if (!languagesDir.exists()) {
				languagesDir.mkdirs();
			}
			
			File lang_file = new File(AdminGuiIntegration.getInstance().getDataFolder(), "Languages/" + language + ".yml");
			if (!lang_file.exists()) {
				try {
					// Extract from JAR resources to AdminGUI Languages folder
					AdminGuiIntegration.getInstance().getBanManagerPlugin().saveResource("admingui/Languages/" + language + ".yml", false);
				} catch (Exception e) {
					AdminGuiIntegration.getInstance().getLogger().warning("Failed to extract language file " + language + ".yml: " + e.getMessage());
					return false;
				}
				getLanguages();
			}
			return true;
		}
		return false;
	}

	public static boolean fixLanguage(String language) {
		if (default_languages.contains(language) && enabled_languages.contains(language)) {
			try {
				// Create Languages directory if it doesn't exist
				File languagesDir = new File(AdminGuiIntegration.getInstance().getDataFolder(), "Languages");
				if (!languagesDir.exists()) {
					languagesDir.mkdirs();
				}
				
				// Extract from JAR resources and overwrite existing
				AdminGuiIntegration.getInstance().getBanManagerPlugin().saveResource("admingui/Languages/" + language + ".yml", true);
				
				getLanguages();
				return true;
			} catch (Exception e) {
				AdminGuiIntegration.getInstance().getLogger().warning("Failed to fix language file " + language + ".yml: " + e.getMessage());
				return false;
			}
		}
		return false;
	}

	public static boolean advancedFixLanguage(String language) {
		if (enabled_languages.contains(language)) {
			YamlConfiguration temp_lang = new YamlConfiguration();
			YamlConfiguration lang = languages.getOrDefault(language, null);
			YamlConfiguration ml_lang = new YamlConfiguration();

			InputStream lang_inputStream = AdminGuiIntegration.getInstance().getBanManagerPlugin().getResource("admingui/Languages/" + language + ".yml");
			if (lang_inputStream == null) lang_inputStream = AdminGuiIntegration.getInstance().getBanManagerPlugin().getResource("admingui/Languages/English.yml");

			try {
				temp_lang.load(new InputStreamReader(lang_inputStream));
			} catch (InvalidConfigurationException | IOException e) {
				e.printStackTrace();
			}

			Set<String> temp_lang_keys = temp_lang.getKeys(false);
			Set<String> lang_keys = lang.getKeys(false);

			if (!temp_lang_keys.equals(lang_keys)) {
				for (String key : temp_lang_keys) {
					if (!lang_keys.contains(key)) ml_lang.set(key, temp_lang.getString(key, ""));
				}
				try {
					ml_lang.save(new File(AdminGuiIntegration.getInstance().getDataFolder(), "Languages/" + language + "-Missing-Lines.txt"));
				} catch (IOException ignored) {
				}
			}
			return true;
		}
		return false;
	}

	public static boolean isLangFile(String language) {
		YamlConfiguration lang = new YamlConfiguration();
		File lang_file = new File(AdminGuiIntegration.getInstance().getDataFolder(), "Languages/" + language);
		if (lang_file.exists()) {
			try {
				lang.load(lang_file);
				if (lang.isString("prefix")) {
					languages.put(language.replace(".yml", ""), lang);
					return true;
				}
			} catch (IOException | InvalidConfigurationException ignored) {
			}
		}
		return false;
	}

}
