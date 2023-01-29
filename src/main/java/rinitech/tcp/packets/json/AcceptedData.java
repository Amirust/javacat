package rinitech.tcp.packets.json;

import rinitech.tcp.Room;

public class AcceptedData
{
	/**
	 * The http address of the server.
	 */
	public String http;
	/**
	 * All available rooms in the server.
	 */
	public Room[] rooms = new Room[]{};
}
