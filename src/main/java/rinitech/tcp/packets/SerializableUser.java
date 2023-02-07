package rinitech.tcp.packets;

import rinitech.database.types.User;

public class SerializableUser
{
	public long id;
	public String username;
	public String avatar;

	public SerializableUser(User user)
	{
		this.id = user.id;
		this.username = user.username;
		this.avatar = user.avatar;
	}
}
