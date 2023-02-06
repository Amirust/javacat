package rinitech.tcp;

import rinitech.tcp.packets.MCPPacket;

import java.util.ArrayList;

public class Room
{
	private int id;
	private String name;
	private ArrayList<ServerClient> users = new ArrayList<>();

	public Room(int id, String name)
	{
		this.id = id;
		this.name = name;
	}
	static public Room fromId(int id)
	{
		for (Room room : Server.getRooms())
		{
			if (room.id == id) return room;
		}
		return null;
	}

	public void broadcast(MCPPacket packet)
	{
		for (ServerClient user : users)
		{
			user.send(packet, true);
		}
	}

	public void addUser(ServerClient serverClient)
	{
		users.add(serverClient);
	}

	public void removeUser(ServerClient serverClient)
	{
		users.remove(serverClient);
	}

	public int getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public ArrayList<ServerClient> getUsers()
	{
		return users;
	}
}
