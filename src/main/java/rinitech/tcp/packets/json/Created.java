package rinitech.tcp.packets.json;

/**
 * Created packet sends to the client when the user has successfully created a room.
 * Data contains the id and name of the room.
 * @see CreatedData
 */
public class Created extends BasePackage
{
	public CreatedData data;
}

class CreatedData
{
	/**
	 * The id of the room.
	 */
	public int id;
	/**
	 * The name of the room.
	 */
	public String name;
}
