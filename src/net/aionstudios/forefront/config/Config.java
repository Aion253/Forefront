package net.aionstudios.forefront.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

import org.json.JSONException;
import org.json.JSONObject;

import net.aionstudios.forefront.util.SecurityUtils;

public class Config {
	
	private JSONObject config = new JSONObject();
	private File configFile;
	private String[] keys;
	
	public Config(File confFile) {
		configFile = confFile;
		config = SecurityUtils.getLinkedJsonObject();
	}
	
	public void setRequiredKeys(String... keys) {
		this.keys = keys;
	}
	
	public boolean readConfig() {
		JSONObject c = readFile();
		for(String key : keys) {
			if(!c.has(key)) {
				return false;
			}
		}
		config = c;
		return true;
	}
	
	public boolean writeConfig() {
		try {
			if(!configFile.exists()) {
				configFile.getParentFile().mkdirs();
				configFile.createNewFile();
				System.out.println("Created config file '"+configFile.toString()+"'!");
			}
			PrintWriter writer;
			File temp = File.createTempFile("temp_andf", null, configFile.getParentFile());
			writer = new PrintWriter(temp.toString(), "UTF-8");
			writer.println(config.toString(2));
			writer.close();
			Files.deleteIfExists(configFile.toPath());
			temp.renameTo(configFile);
			return true;
		} catch (IOException e) {
			System.err.println("Encountered an IOException while writing config: '"+configFile.toString()+"'!");
			e.printStackTrace();
			return false;
		} catch (JSONException e) {
			System.err.println("Encountered a JSONException while writing config: '"+configFile.toString()+"'!");
			e.printStackTrace();
			return false;
		}
	}
	
	private JSONObject readFile() {
		if(!configFile.exists()) {
			System.err.println("Failed reading config: '"+configFile.toString()+"'. No such file!");
			return null;
		}
		String jsonString = "";
		try (BufferedReader br = new BufferedReader(new FileReader(configFile.toString()))) {
		    for (String line; (line = br.readLine()) != null;) {
		    	jsonString += line;
		    }
		    br.close();
		    return new JSONObject(jsonString);
		} catch (IOException e) {
			System.err.println("Encountered an IOException while reading config: '"+configFile.toString()+"'!");
			e.printStackTrace();
			return null;
		} catch (JSONException e) {
			System.err.println("Encountered a JSONException while reading config: '"+configFile.toString()+"'!");
			e.printStackTrace();
			return null;
		}
	}

	public JSONObject getConfig() {
		return config;
	}

	public String[] getRequiredKeys() {
		return keys;
	}

	public File getConfigFile() {
		return configFile;
	}

}
