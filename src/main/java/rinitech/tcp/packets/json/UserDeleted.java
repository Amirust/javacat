package rinitech.tcp.packets.json;

/**
 * UserDeleted packet sends to the client when the server has deleted a user.
 * Data contains the username of the user.
 * @see UserDeletedData
 */
public class UserDeleted extends BasePackage
{
	public UserDeletedData data;
}
