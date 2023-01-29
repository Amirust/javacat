package rinitech.tcp.packets.json;

/**
 * Joined packet sends to the client when the client joins a room.
 * Data contains the id of the room.
 * @see JoinedData
 */
public class Joined extends BasePackage
{
	public JoinedData data;
}

