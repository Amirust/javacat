package rinitech.tcp.packets.json;

import rinitech.tcp.Room;

/**
 * Accepted packet sends to the client when the client has successfully authorized.
 * Data contains the http address of the server and the rooms the client is in.
 * @see AcceptedData
 */
public class Accepted extends BasePackage
{
	public AcceptedData data;
}

/**
 * Data of the Accepted packet.
 */
class AcceptedData
{
	/**
	 * The http address of the server.
	 */
	public String http;
	/**
	 * All available rooms in the server.
	 */
	public Room[] rooms;
}