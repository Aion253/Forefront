package net.aionstudios.forefront.nodes;

import java.util.ArrayList;
import java.util.List;

public class DomainManager {
	
	private static List<ForefrontDomain> domains = new ArrayList<ForefrontDomain>();
	
	public static void addDomain(ForefrontDomain domain) {
		for(ForefrontDomain d : domains) {
			if(d.getDomain()==domain.getDomain()) {
				System.err.println("Cannot register duplicate domain '"+d.getDomain()+"'");
				System.err.println("Shutting down...");
				System.exit(0);
			}
		}
		domains.add(domain);
	}
	
	public static ForefrontDomain findDomain(String domain) {
		for(ForefrontDomain d : domains) {
			if(d.getDomain().equalsIgnoreCase(domain)) {
				return d;
			}
		}
		return null;
	}

}
