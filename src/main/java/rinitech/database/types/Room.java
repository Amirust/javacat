package rinitech.database.types;

public class Room
{
	public String name;
	public int id;

	public rinitech.tcp.Room toMcpRoom() {
		return new rinitech.tcp.Room(id, name);
	}
}
