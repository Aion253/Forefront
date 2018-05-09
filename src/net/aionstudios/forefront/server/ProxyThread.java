package net.aionstudios.forefront.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;


import net.aionstudios.forefront.nodes.DomainManager;
import net.aionstudios.forefront.nodes.ForefrontDomain;
import net.aionstudios.forefront.nodes.ForefrontNode;

public class ProxyThread extends Thread {
    
	private Socket socket = null;
    public ProxyThread(Socket socket) {
        super("ProxyThread");
        this.socket = socket;
    }

    public void run() {
        //get input from user
        //send request to server
        //get response from server
        //send response to user

        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String inputLine;
            int cnt = 0;
            String method = "";
            String urlToCall = "";
            Map<String, String> headers = new HashMap<String, String>();
            while ((inputLine = in.readLine()) != null) {
                try {
                    StringTokenizer tok = new StringTokenizer(inputLine);
                    tok.nextToken();
                } catch (Exception e) {
                    break;
                }
                if (cnt == 0) {
                    String[] tokens = inputLine.split(" ");
                    urlToCall = tokens[1];
                    method = tokens[0];
                }
                cnt++;
                String[] separated = inputLine.split(": ", 2);
                if(separated.length==2) {
                	if(separated[0]!="Host"&&separated[0]!="Upgrade-Insecure-Requests")
                		headers.put(separated[0], separated[1]);
                }
            }
            try {
                ForefrontDomain externalDomain = DomainManager.findDomain(headers.get("Host"));
                ForefrontNode optimalNode = null;
                double lowest = 100.0;
                for(ForefrontNode n : externalDomain.getNodeEndpointPairs().keySet()) {
                	if(n.optimalCalculation()<lowest) {
                		optimalNode = n;
                	}
                }
                String endpoint = externalDomain.getNodeEndpointPairs().get(optimalNode)+urlToCall;
            	URL url = new URL(endpoint);
            	HttpURLConnection.setFollowRedirects(false);
            	HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            	conn.setRequestMethod(method);
                for(int i = 0; i < headers.size(); i++) {
                	conn.setRequestProperty((String) headers.keySet().toArray()[i], headers.get(headers.keySet().toArray()[i]));
                }
                if(!headers.containsKey("User-Agent")) {
                	conn.setRequestProperty("User-Agent", "AionForefrontProxy/1.0.0");
                } else {
                	conn.setRequestProperty("User-Agent", headers.get("User-Agent")+" AionForefrontProxy/1.0.0");
                }
                conn.setRequestProperty("Accept-Encoding", "deflate");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("X-Forwarded-For", socket.getInetAddress().toString().replace("/", ""));
                conn.setDoOutput(true);
                Reader inp = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                
                StringBuilder sb = new StringBuilder();
                Map<String, List<String>> map = conn.getHeaderFields();
            	for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            		if(entry.getKey()!=null&&entry.getKey().startsWith("Transfer-E")) {
            			continue;
            		}
            		if(entry.getKey()==null) {
            			if(entry.getValue().size()>1) {
            				String value = entry.getValue()+"";
            				if(value.startsWith("[")) {
            					value = value.substring(1);
            				}
            				if(value.endsWith("]")) {
            					value = value.substring(0, value.length()-1);
            				}
                			sb.append(value+"\r\n");
            			} else {
                			sb.append(entry.getValue().get(0)+"\r\n");
            			}
            		} else {
            			if(entry.getValue().size()>1) {
            				String value = entry.getValue()+"";
            				if(value.startsWith("[")) {
            					value = value.substring(1);
            				}
            				if(value.endsWith("]")) {
            					value = value.substring(0, value.length()-1);
            				}
                			sb.append(entry.getKey() + 
                                    ": " + value+"\r\n");
            			} else {
                			sb.append(entry.getKey() + 
                                    ": " + entry.getValue().get(0)+"\r\n");
            			}
            		}
            	}
            	sb.append("\r\n");
                for (int c; (c = inp.read()) >= 0;) {
                	sb.append((char) c);
                }
                out.writeBytes(sb.toString());
            } catch (Exception e) {
                out.writeBytes("Connection failed.");
            }
            
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null) {
                socket.close();
            }

        } catch (IOException e) {
            
        }
    }
}
