package rinitech.database;

import rinitech.database.types.ImageMessage;
import rinitech.database.types.Room;
import rinitech.database.types.TextMessage;
import rinitech.database.types.User;

import java.util.Date;

public interface DatabaseAdapter
{
	// User
	public User getUser(String username);
	public boolean addUser(String username, String password);
	public boolean updateUserPassword(String username, String password);
	public boolean updateUserAvatar(String username, String avatar);
	public boolean deleteUser(String username);

	// Room
	public Room[] getRooms();
	public Room getRoom(String name);
	public Room getRoom(int id);
	public boolean addRoom(String name, int id);
	public boolean updateRoom(int id, String name);
	public boolean deleteRoom(String name);
	public boolean deleteRoom(int id);

	// TextMessage
	public TextMessage[] getTextMessages(int roomId);
	public TextMessage getTextMessage(long id);
	public boolean addTextMessage(String sender, String content, int roomId, Date date);
	public boolean deleteTextMessage(long id);

	// ImageMessage
	public ImageMessage[] getImageMessages(int roomId);
	public ImageMessage getImageMessage(long id);
	public boolean addImageMessage(String sender, String url, int roomId, Date date);
	public boolean deleteImageMessage(long id);

	// All messages
	public Object[] getMessages(int roomId);
}
