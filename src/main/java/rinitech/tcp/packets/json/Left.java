package rinitech.tcp.packets.json;

/**
 * Left packet sends to the client when the user leaves a room.
 * Data contains the id of the room.
 * @see LeftData
 */
public class Left extends BasePackage
{
	public LeftData data;
}

class LeftData
{
	/**
	 * The id of the room.
	 */
	public int room;

	/**
	 * The username of the user.
	 */
	public String user;
}
