package rinitech.tcp.packets.json;

/**
 * Join packet sends to the server when the client wants to join a room.
 * Data contains the id of the room.
 * @see JoinData
 */
public class Join extends BasePackage
{
	public JoinData data;
}

