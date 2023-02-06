package rinitech.tcp.packets.json;

/**
 * UserCreated packet sends to the client when the server has created a user.
 * Data contains the username of the user.
 * @see UserCreatedData
 */
public class UserCreated extends BasePackage
{
	public UserCreatedData data;
}
