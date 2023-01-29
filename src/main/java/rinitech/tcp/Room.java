package rinitech.tcp;

import static rinitech.tcp.Server.rooms;

public class Room
{
	public int id;
	public String name;

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
}
