package rinitech.tcp.packets.json;

import rinitech.tcp.Room;

/**
 * List packet sends to the client when the server sends the list of rooms.
 * Data contains the list of rooms.
 * @see ListData
 */
public class List extends BasePackage
{
	public ListData data;
}

class ListData
{
	/**
	 * The list of rooms.
	 */
	public Room[] rooms;
}
