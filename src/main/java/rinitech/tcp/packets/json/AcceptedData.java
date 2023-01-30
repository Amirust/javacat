package rinitech.tcp.packets.json;

import rinitech.tcp.packets.SerializableRoom;

public class AcceptedData
{
	/**
	 * The http address of the server.
	 */
	public String http;
	/**
	 * All available rooms in the server.
	 */
	public SerializableRoom[] rooms = new SerializableRoom[]{};
}
