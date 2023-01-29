package rinitech.tcp.packets.json;

/**
 * Updated packet sends to the client when the server updates a room.
 * Data contains the id of the room.
 * @see UpdatedData
 */
public class Updated extends BasePackage
{
	public UpdatedData data;
}

