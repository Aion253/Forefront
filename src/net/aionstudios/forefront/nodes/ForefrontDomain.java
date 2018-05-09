package net.aionstudios.forefront.nodes;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.aionstudios.forefront.ForefrontInfo;
import net.aionstudios.forefront.config.Config;
import net.aionstudios.forefront.util.SecurityUtils;

public class ForefrontDomain {
	
	private String domain;
	private String requestRoot;
	private boolean clientAffine;
	
	private Map<ForefrontNode, String> nodeEndpointPairs = new HashMap<ForefrontNode, String>();
	
	public ForefrontDomain(String domainString) {
		File domainRoot;
		Config domainConfig;
		Config endpointsConfig;
		domain = domainString;
		String domainFile = domain.replace(":", "_");
		domainFile = ForefrontInfo.FFT_CONFIG_BASE+"domains/"+domainFile;
		domainRoot = new File(domainFile);
		boolean incompleteConfigs = false;
		if(!domainRoot.exists()) {
			domainRoot.mkdirs();
			System.err.println("Domain directory '"+domainFile+"' generated. Shutting down to setup.");
			incompleteConfigs = true;
		}
		domainConfig = new Config(new File(domainFile+"/domain.json"));
		if(!domainConfig.getConfigFile().exists()) {
			try {
				domainConfig.getConfigFile().createNewFile();
				domainConfig.getConfig().put("address", domainString);
				domainConfig.getConfig().put("request_root", "");
				domainConfig.getConfig().put("client_affine", false);
				domainConfig.writeConfig();
				System.err.println("Domain config '"+domainFile+"/domain.json' empty. Shutting down to setup.");
				incompleteConfigs = true;
			} catch (IOException e) {
				System.err.println("Encountered an IOException creating config file '"+domainFile+"/domain.json'");
				e.printStackTrace();
			} catch (JSONException e) {
				System.err.println("Encountered a JSONException creating config file '"+domainFile+"/domain.json'");
				e.printStackTrace();
			}
		}
		endpointsConfig = new Config(new File(domainFile+"/endpoints.json"));
		if(!endpointsConfig.getConfigFile().exists()) {
			try {
				endpointsConfig.getConfigFile().createNewFile();
				JSONArray ja = new JSONArray();
				JSONObject endpoint = SecurityUtils.getLinkedJsonObject();
				endpoint.put("forefront_node", "x.x.x.x:26736");
				endpoint.put("forefront_endpoint", "x.x.x.x:80");
				endpoint.put("endpoint_secure", false);
				endpoint.put("security_key", "");
				endpoint.put("security_secret", "");
				ja.put(endpoint);
				endpointsConfig.getConfig().put("points", ja);
				endpointsConfig.writeConfig();
				System.err.println("Endpoint config '"+domainFile+"/endpoints.json' empty. Shutting down to setup.");
				incompleteConfigs = true;
			} catch (IOException e) {
				System.err.println("Encountered an IOException creating config file '"+domainFile+"/endpoints.json'");
				e.printStackTrace();
			} catch (JSONException e) {
				System.err.println("Encountered a JSONException creating config file '"+domainFile+"/endpoints.json'");
				e.printStackTrace();
			}
		}
		if(incompleteConfigs) {
			System.exit(0);
		}
		domainConfig.setRequiredKeys("address", "request_root", "client_affine");
		endpointsConfig.setRequiredKeys("points");
		domainConfig.readConfig();
		endpointsConfig.readConfig();
		try {
			domain = domainConfig.getConfig().getString("address");
			requestRoot = domainConfig.getConfig().getString("request_root");
			clientAffine = domainConfig.getConfig().getBoolean("client_affine");
		} catch (JSONException e) {
			System.err.println("Encountered a JSONException reading config file '"+domainFile+"/domain.json'");
			e.printStackTrace();
		}
		try {
			JSONArray nePairs = endpointsConfig.getConfig().getJSONArray("points");
			for(int i = 0; i < nePairs.length(); i++) {
				JSONObject j = nePairs.getJSONObject(i);
				String nodeString = j.getString("forefront_node");
				String sK = j.getString("security_key");
				String sS = j.getString("security_secret");
				boolean cS = j.getBoolean("endpoint_secure");
				ForefrontNode ffnode = new ForefrontNode(nodeString, sK, sS);
				String endpointString = j.getString("forefront_endpoint");
				if(endpointString.startsWith("http://")) {
					endpointString = endpointString.replaceFirst("http://", "");
				} else if(endpointString.startsWith("https://")) {
					endpointString = endpointString.replaceFirst("https://", "");
				}
				if(cS) {
					endpointString = "https://"+endpointString;
				} else {
					endpointString = "http://"+endpointString;
				}
				if(nodeEndpointPairs.containsKey(ffnode)) {
					System.err.println("Duplicate node '"+nodeString+"' for domain '"+domain+"'");
				}
				if(nodeEndpointPairs.containsValue(endpointString)) {
					System.err.println("Duplicate endpoint '"+endpointString+"' for domain '"+domain+"'");
				}
				nodeEndpointPairs.put(ffnode, endpointString);
			}
		} catch (JSONException e) {
			System.err.println("Encountered a JSONException reading config file '"+domainFile+"/endpoints.json'");
			e.printStackTrace();
		}
		DomainManager.addDomain(this);
	}

	public String getDomain() {
		return domain;
	}

	public String getRequestRoot() {
		return requestRoot;
	}

	public boolean isClientAffine() {
		return clientAffine;
	}

	public Map<ForefrontNode, String> getNodeEndpointPairs() {
		return nodeEndpointPairs;
	}

}
