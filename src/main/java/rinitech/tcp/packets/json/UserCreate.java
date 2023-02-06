package rinitech.tcp.packets.json;

/**
 * UserCreate packet sends to the server when the client wants to create a new user.
 * Data contains the username and password of the user.
 * @see UserCreateData
 */
public class UserCreate extends BasePackage
{
	public UserCreateData data;
}
