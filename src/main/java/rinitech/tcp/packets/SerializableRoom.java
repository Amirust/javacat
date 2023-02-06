package rinitech.tcp.packets;

import rinitech.tcp.ServerClient;
import rinitech.tcp.Room;

import java.util.ArrayList;

public class SerializableRoom
{
	public int id;
	public String name;
	public ArrayList<String> allUsers = new ArrayList<>();

	public SerializableRoom(Room room)
	{
		this.id = room.getId();
		this.name = room.getName();
		for (ServerClient user : room.getUsers())
		{
			allUsers.add(user.getUsername());
		}
	}
}
