package rinitech.tcp.packets.json;

/**
 * Deleted packet sends to the client when the server deletes a room.
 * Data contains the id of the room.
 * @see DeletedData
 */
public class Deleted extends BasePackage
{
	public DeletedData data;
}

class DeletedData
{
	/**
	 * The id of the room.
	 */
	public int room;
}
