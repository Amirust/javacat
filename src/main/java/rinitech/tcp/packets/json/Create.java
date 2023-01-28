package rinitech.tcp.packets.json;

/**
 * Create packet sends to the server when the client wants to create a new room.
 * Data contains the name of the room.
 * @see CreateData
 */
public class Create extends BasePackage
{
	public CreateData data;
}

class CreateData
{
	/**
	 * The name of the room.
	 */
	public String name;
}
