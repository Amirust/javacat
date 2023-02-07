package rinitech.tcp.packets.json;

import rinitech.tcp.packets.SerializableUser;

public class JoinedData
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
	public SerializableUser[] allUsers = new SerializableUser[]{};
}
