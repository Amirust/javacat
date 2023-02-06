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
	private static final ArrayList<ServerClient> serverClients = new ArrayList<>();
	private static final ArrayList<Room> rooms = new ArrayList<>();
	private DiffieHellman DH = new DiffieHellman();
	private DatabaseAdapter database;
	private Config config;
	private boolean isClosed = false;

	public Server(Config config, DatabaseAdapter database, int port) throws IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException
	{
		this.config = config;
		this.database = database;
		for (rinitech.database.types.Room room : database.getRooms()) {
			rooms.add(new Room(room.id, room.name));
		}
		serverSocket = new ServerSocket(port);
	}

	public void start()
	{
		System.out.println("Server started on port " + serverSocket.getLocalPort());
		while (!isClosed)
		{
			try {
				Socket socket = serverSocket.accept();
				System.out.println("Client connected from " + socket.getInetAddress().getHostAddress());
				try {
					ServerClient serverClient = ServerClient.fromSocket(socket, this);
					addClient(serverClient);
					serverClient.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (IOException ignored) {}
		}
	}

	public void stop() throws IOException
	{
		isClosed = true;
		serverSocket.close();
		for (ServerClient serverClient : serverClients)
		{
			serverClient.stopClient();
		}
	}

	public void addClient(ServerClient serverClient)
	{
		serverClients.add(serverClient);
	}

	public void removeClient(ServerClient serverClient)
	{
		serverClients.remove(serverClient);
		rooms.forEach(room -> room.removeUser(serverClient));
	}

	public static ArrayList<Room> getRooms()
	{
		return rooms;
	}

	public static ArrayList<ServerClient> getClients()
	{
		return serverClients;
	}

	public DiffieHellman getDH()
	{
		return DH;
	}

	public DatabaseAdapter getDatabase()
	{
		return database;
	}

	public Config getConfig()
	{
		return config;
	}
}
