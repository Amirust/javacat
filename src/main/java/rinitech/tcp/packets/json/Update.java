package rinitech.tcp.packets.json;

/**
 * Update packet sends to the client when the server updates a room.
 * Data contains the id of the room.
 * @see UpdateData
 */
public class Update extends BasePackage
{
	public UpdateData data;
}

