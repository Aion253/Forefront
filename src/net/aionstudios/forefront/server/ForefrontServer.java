package net.aionstudios.forefront.server;

import java.io.IOException;
import java.net.ServerSocket;

public class ForefrontServer {

	public ForefrontServer(int port) throws IOException {
		ServerSocket serverSocket = null;
        boolean listening = true;

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);
        } catch (IOException e) {
            System.err.println("Could not listen on port " + port);
            System.exit(-1);
        }

        while (listening) {
            new ProxyThread(serverSocket.accept()).start();
        }
        serverSocket.close();
	}
	
}
