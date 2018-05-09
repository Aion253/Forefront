package net.aionstudios.forefront;

import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;

import net.aionstudios.aionlog.AnsiOut;
import net.aionstudios.aionlog.Logger;
import net.aionstudios.aionlog.StandardOverride;
import net.aionstudios.forefront.config.Config;
import net.aionstudios.forefront.cron.CronManager;
import net.aionstudios.forefront.crons.CronUpdate;
import net.aionstudios.forefront.nodes.ForefrontDomain;
import net.aionstudios.forefront.server.ForefrontServer;

public class Forefront {
	
	private static Config rootConfig;
	
	public static void main(String[] args) throws JSONException, IOException {
		System.setProperty("java.net.preferIPv4Stack" , "true");
		System.out.println("Starting Forefront...");
		setupDefaults();
		if(((boolean) rootConfig.getConfig().get("log_enabled"))) {
			File f = new File("./logs/");
			f.mkdirs();
			Logger.setup();
		}
		JSONArray identities = (JSONArray) rootConfig.getConfig().get("domain_identities");
		for(int i = 0; i<identities.length();i++) {
			new ForefrontDomain((String) identities.get(i));
		}
		AnsiOut.initialize();
		AnsiOut.setStreamPrefix("Forefront");
		StandardOverride.enableOverride();
		new ForefrontServer(80);
	}
	
	private static void setupDefaults() {
		readConfigs();
		
		/*Cron*/
		CronManager.addJob(new CronUpdate());
		CronManager.startCron(5000);
	}
	
	public static boolean readConfigs() {
		try {
			File rtcf = new File(ForefrontInfo.FFT_ROOT_CONFIG);
			rootConfig = new Config(rtcf);
			if(!rtcf.exists()) {
				rtcf.getParentFile().mkdirs();
				rtcf.createNewFile();
				System.err.println("Fill out the config at "+rtcf.getCanonicalPath()+" and restart the server!");
				JSONArray jsonServerArray = new JSONArray();
				jsonServerArray.put("default");
				jsonServerArray.put("example.com");
				rootConfig.getConfig().put("accept_port", 80);
				rootConfig.getConfig().put("accept_port_secure", 443);
				rootConfig.getConfig().put("log_enabled", true);
				rootConfig.getConfig().put("domain_identities", jsonServerArray);
				rootConfig.writeConfig();
				System.exit(0);
			} else {
				rootConfig.setRequiredKeys("accept_port", "accept_port_secure", "log_enabled", "log_enabled", "domain_identities");
				if(!rootConfig.readConfig()) {
					System.out.println("Missing or malformed config data! Ensure all variables are set. "+rootConfig.getRequiredKeys());
					System.exit(0);
				}
			}
			return true;
		} catch (IOException e) {
			System.err.println("Encountered an IOException during config file operations!");
			e.printStackTrace();
			return false;
		} catch (JSONException e) {
			System.err.println("Encountered an JSONException during config file operations!");
			e.printStackTrace();
			return false;
		}
	}

}
