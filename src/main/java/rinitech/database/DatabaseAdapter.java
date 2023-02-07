package rinitech.database;

import rinitech.database.types.Room;
import rinitech.database.types.User;

public interface DatabaseAdapter
{
	// User
	public User getUser(String username);
	public User getUser(long id);
	public boolean addUser(String username, String password);
	public boolean updateUserPassword(String username, String password);
	public boolean updateUserPassword(long id, String password);
	public boolean updateUserAvatar(String username, String avatar);
	public boolean updateUserAvatar(long id, String avatar);
	public boolean updateUserUsername(String username, String newUsername);
	public boolean updateUserUsername(long id, String newUsername);
	public boolean deleteUser(String username);
	public boolean deleteUser(long id);

	// Room
	public Room[] getRooms();
	public Room getRoom(String name);
	public Room getRoom(int id);
	public boolean addRoom(String name, int id);
	public boolean updateRoom(int id, String name);
	public boolean deleteRoom(String name);
	public boolean deleteRoom(int id);

	// Close DB
	public void close();
}
