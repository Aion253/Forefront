package net.aionstudios.forefront.nodes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class ForefrontNode {
	
	private String nodeAddress;
	private String nodeName = "";
	
	private String secureKey;
	private String secureSecret;
	private String token = "";
	
	private int connections = 0;
	private double ramUsed = 0.0;
	private double ramTotal = 0.0;
	private double cpuLoad = 0.0;
	
	public ForefrontNode(String node, String securityKey, String securitySecret) {
		nodeAddress = node;
		secureKey = securityKey;
		secureSecret = securitySecret;
		NodeManager.addNode(this);
	}
	
	public void update() {
		if(token.length()<1) {
			try {
				URL url = new URL("http://"+nodeAddress);
				Map<String, Object> params = new LinkedHashMap<String, Object>();
				params.put("action", "login");
				params.put("key", secureKey);
				params.put("secret", secureSecret);
				
				StringBuilder postData = new StringBuilder();
				for (Map.Entry<String, Object> param : params.entrySet()) {
					if(postData.length()!=0) postData.append('&');
					postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
					postData.append('=');
					postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
				}
				byte[] postDataBytes = postData.toString().getBytes("UTF-8");
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
				conn.setDoOutput(true);
				conn.getOutputStream().write(postDataBytes);
				
				Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
				
				StringBuilder response = new StringBuilder();
				for (int c; (c = in.read()) >= 0;)
					response.append((char)c);
				JSONObject resp = new JSONObject(response.toString());
				if(!resp.isNull("server_name")&&!resp.isNull("token")) {
					nodeName = resp.getString("server_name");
					token = resp.getString("token");
					System.out.println("Connected to node '"+nodeName+" ("+nodeAddress+")'");
					update();
				}
			} catch (ConnectException e) {
				
			} catch (IOException e) {
				System.err.println("Encountered a IOException updating from Node");
				e.printStackTrace();
			} catch (JSONException e) {
				System.err.println("Encountered a JSONException updating from Node");
				e.printStackTrace();
			}
		} else {
			try {
				URL url = new URL("http://"+nodeAddress);
				Map<String, Object> params = new LinkedHashMap<String, Object>();
				params.put("action", "update");
				params.put("token", token);
				
				StringBuilder postData = new StringBuilder();
				for (Map.Entry<String, Object> param : params.entrySet()) {
					if(postData.length()!=0) postData.append('&');
					postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
					postData.append('=');
					postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
				}
				byte[] postDataBytes = postData.toString().getBytes("UTF-8");
				
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
				conn.setDoOutput(true);
				conn.getOutputStream().write(postDataBytes);
				Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
				
				StringBuilder response = new StringBuilder();
				for (int c; (c = in.read()) >= 0;)
					response.append((char)c);
				JSONObject resp = new JSONObject(response.toString());
				if(!resp.isNull("ram_total")&&!resp.isNull("ram_used")&&!resp.isNull("cpu_load")) {
					ramUsed = resp.getDouble("ram_total");
					ramTotal = resp.getDouble("ram_used");
					cpuLoad = resp.getDouble("cpu_load");
				}
			} catch (ConnectException e) {
				
			} catch (JSONException e) {
				System.err.println("Encountered a JSONException updating from Node");
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("Encountered a IOException updating from Node");
				e.printStackTrace();
			}
		}
	}
	
	public double optimalCalculation() {
		return ramUsed/ramTotal*cpuLoad;
	}

	public String getNodeAddress() {
		return nodeAddress;
	}

	public int getConnections() {
		return connections;
	}
	
	public void addConnection() {
		connections++;
	}
	
	public void removeConnection() {
		connections--;
	}

	public double getRamUsed() {
		return ramUsed;
	}

	public double getRamTotal() {
		return ramTotal;
	}

	public double getCpuLoad() {
		return cpuLoad;
	}

}
