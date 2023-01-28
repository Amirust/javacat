package rinitech.tcp.packets.json;

/**
 * Delete packet sends to the server when the client wants to delete a room.
 * Data contains the id of the room.
 * @see DeleteData
 */
public class Delete extends BasePackage
{
	public DeleteData data;
}

class DeleteData
{
	/**
	 * The id of the room.
	 */
	public int room;
}
