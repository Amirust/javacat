package rinitech.tcp;

import rinitech.tcp.packets.MCPPacket;

import java.util.ArrayList;

import static rinitech.tcp.Server.rooms;

public class Room
{
	public int id;
	public String name;
	public ArrayList<ServerClient> users = new ArrayList<>();

	public Room(int id, String name)
	{
		this.id = id;
		this.name = name;
	}
	static public Room fromId(int id)
	{
		for (Room room : rooms)
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
}
