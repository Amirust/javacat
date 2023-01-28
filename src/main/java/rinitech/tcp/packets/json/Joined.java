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

class JoinedData
{
	/**
	 * The id of the room.
	 */
	public int room;

	/**
	 * The username of the user.
	 */
	public String user;

	/**
	 * The list of all users in the room.
	 */
	public String[] allUsers = new String[]{};
}