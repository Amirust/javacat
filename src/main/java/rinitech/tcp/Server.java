package rinitech.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Server
{
	private ServerSocket serverSocket;
	public static final ArrayList<Client> clients = new ArrayList<>();
	public static final ArrayList<Room> rooms = new ArrayList<>();
	public DiffieHellman DH = new DiffieHellman();

	public Server(int port) throws IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException
	{
		serverSocket = new ServerSocket(port);
	}

	public void start() throws IOException
	{
		System.out.println("Server started on port " + serverSocket.getLocalPort());
		while (!serverSocket.isClosed())
		{
			Socket socket = serverSocket.accept();
			System.out.println("Client connected from " + socket.getInetAddress().getHostAddress());
			try {
				Client client = Client.fromSocket(socket, this);
				addClient(client);
				client.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void stop() throws IOException
	{
		serverSocket.close();
	}

	public void addClient(Client client)
	{
		clients.add(client);
	}
}
