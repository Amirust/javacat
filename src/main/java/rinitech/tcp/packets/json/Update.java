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

class UpdateData
{
	/**
	 * The id of the room.
	 */
	public int room;

	/**
	 * The new name of the room.
	 */
	public String name;
}
