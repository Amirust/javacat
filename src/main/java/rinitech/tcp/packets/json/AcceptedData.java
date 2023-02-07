package rinitech.tcp.packets.json;

import rinitech.tcp.packets.SerializableRoom;

public class AcceptedData
{
	/**
	 * All available rooms in the server.
	 */
	public SerializableRoom[] rooms = new SerializableRoom[]{};
	public int heartbeatRate;
}
