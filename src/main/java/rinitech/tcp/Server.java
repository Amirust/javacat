package rinitech.tcp;

import rinitech.config.Config;
import rinitech.database.DatabaseAdapter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Server
{
	private final ServerSocket serverSocket;
	public final ArrayList<ServerClient> serverClients = new ArrayList<>();
	public static final ArrayList<Room> rooms = new ArrayList<>();
	public DiffieHellman DH = new DiffieHellman();
	public DatabaseAdapter database;
	public Config config;

	public Server(Config config, DatabaseAdapter database, int port) throws IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException
	{
		this.config = config;
		this.database = database;
		for (rinitech.database.types.Room room : database.getRooms()) {
			rooms.add(new Room(room.id, room.name));
		}
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
				ServerClient serverClient = ServerClient.fromSocket(socket, this);
				addClient(serverClient);
				serverClient.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void stop() throws IOException
	{
		serverSocket.close();
	}

	public void addClient(ServerClient serverClient)
	{
		serverClients.add(serverClient);
	}
}
