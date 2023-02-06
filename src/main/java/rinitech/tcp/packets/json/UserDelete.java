package rinitech.tcp.packets.json;

/**
 * UserDelete packet sends to the server when the client wants to delete a user.
 * Data contains the username and password of the user.
 * @see UserDeleteData
 */
public class UserDelete extends BasePackage
{
	public UserDeleteData data;
}