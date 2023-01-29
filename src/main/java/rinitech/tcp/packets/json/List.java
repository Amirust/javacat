package rinitech.tcp.packets.json;

/**
 * List packet sends to the client when the server sends the list of rooms.
 * Data contains the list of rooms.
 * @see ListData
 */
public class List extends BasePackage
{
	public ListData data;
}

