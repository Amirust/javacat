package rinitech.database.implementations;

import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteCollection;
import rinitech.database.DatabaseAdapter;
import rinitech.database.types.ImageMessage;
import rinitech.database.types.Room;
import rinitech.database.types.TextMessage;
import rinitech.database.types.User;

import java.util.Date;

import static org.dizitart.no2.filters.Filters.eq;

public class NitriteAdapter implements DatabaseAdapter
{
	Nitrite db;
	NitriteCollection users;
	NitriteCollection rooms;
	NitriteCollection messages;
	public NitriteAdapter(String dbPath, String userId, String password)
	{
		db = Nitrite.builder()
			.compressed()
			.filePath(dbPath)
			.openOrCreate(userId, password);

		users = db.getCollection("users");
		rooms = db.getCollection("rooms");
		messages = db.getCollection("messages");
	}
	public User getUser(String username)
	{
		Document document = users.find(eq("username", username)).firstOrDefault();
		if (document == null) return null;
		User user = new User();
		user.username = document.get("username", String.class);
		user.password = document.get("password", String.class);
		user.avatar = document.get("avatar", String.class);
		return user;
	}

	public boolean addUser(String username, String password)
	{
		Document doc = Document.createDocument("username", username)
				.put("password", password);

		try {
			users.insert(doc);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean updateUserPassword(String username, String password)
	{
		Document origin = users.find(eq("username", username)).firstOrDefault();
		if (origin == null) return false;
		origin.put("password", password);
		try {
			users.update(origin);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean updateUserAvatar(String username, String avatar)
	{
		Document origin = users.find(eq("username", username)).firstOrDefault();
		if (origin == null) return false;
		origin.put("avatar", avatar);
		try {
			users.update(origin);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean deleteUser(String username)
	{
		Document origin = users.find(eq("username", username)).firstOrDefault();
		if (origin == null) return false;
		try {
			users.remove(origin);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public Room[] getRooms()
	{
		Document[] documents = rooms.find().toList().toArray(new Document[0]);
		Room[] rooms = new Room[documents.length];
		for (int i = 0; i < documents.length; i++)
		{
			rooms[i] = new Room();
			rooms[i].name = documents[i].get("name", String.class);
			rooms[i].id = documents[i].get("id", Integer.class);
		}
		return rooms;
	}

	public Room getRoom(String name)
	{
		Document document = rooms.find(eq("name", name)).firstOrDefault();
		if (document == null) return null;
		Room room = new Room();
		room.name = document.get("name", String.class);
		room.id = document.get("id", Integer.class);
		return room;
	}

	public Room getRoom(int id)
	{
		Document document = rooms.find(eq("id", id)).firstOrDefault();
		if (document == null) return null;
		Room room = new Room();
		room.name = document.get("name", String.class);
		room.id = document.get("id", Integer.class);
		return room;
	}

	public boolean addRoom(String name, int id)
	{
		Document doc = Document.createDocument("name", name)
				.put("id", id);

		try {
			rooms.insert(doc);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean updateRoom(int id, String name)
	{
		Document origin = rooms.find(eq("id", id)).firstOrDefault();
		if (origin == null) return false;
		origin.put("name", name);
		try {
			rooms.update(origin);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean deleteRoom(String name)
	{
		Document origin = rooms.find(eq("name", name)).firstOrDefault();
		if (origin == null) return false;
		try {
			rooms.remove(origin);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean deleteRoom(int id)
	{
		Document origin = rooms.find(eq("id", id)).firstOrDefault();
		if (origin == null) return false;
		try {
			rooms.remove(origin);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public TextMessage[] getTextMessages(int roomId)
	{
		Document[] documents = messages.find(eq("roomId", roomId)).toList().toArray(new Document[0]);
		TextMessage[] messages = new TextMessage[documents.length];
		for (int i = 0; i < documents.length; i++) {
			if (documents[i].get("content", String.class) == null) continue;
			messages[i] = new TextMessage();
			messages[i].sender = documents[i].get("sender", String.class);
			messages[i].content = documents[i].get("content", String.class);
			messages[i].room = documents[i].get("roomId", Integer.class);
			messages[i].date = documents[i].get("date", Date.class);
			messages[i].id = documents[i].get("id", Long.class);
		}
		return messages;
	}

	public TextMessage getTextMessage(long id)
	{
		return null;
	}

	public boolean addTextMessage(String sender, String content, int roomId, Date date)
	{
		long id = messages.size() + 1;
		Document doc = Document.createDocument("sender", sender)
				.put("content", content)
				.put("roomId", roomId)
				.put("date", date)
				.put("id", id);

		try {
			messages.insert(doc);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean deleteTextMessage(long id)
	{
		Document origin = messages.find(eq("id", id)).firstOrDefault();
		if (origin == null) return false;
		try {
			messages.remove(origin);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public ImageMessage[] getImageMessages(int roomId)
	{
		Document[] documents = messages.find(eq("roomId", roomId)).toList().toArray(new Document[0]);
		ImageMessage[] messages = new ImageMessage[documents.length];
		for (int i = 0; i < documents.length; i++) {
			if (documents[i].get("url", String.class) == null) continue;
			messages[i] = new ImageMessage();
			messages[i].sender = documents[i].get("sender", String.class);
			messages[i].url = documents[i].get("url", String.class);
			messages[i].room = documents[i].get("roomId", Integer.class);
			messages[i].date = documents[i].get("date", Date.class);
			messages[i].id = documents[i].get("id", Long.class);
		}
		return messages;
	}

	public ImageMessage getImageMessage(long id)
	{
		return null;
	}

	public boolean addImageMessage(String sender, String url, int roomId, Date date)
	{
		long id = messages.size() + 1;
		Document doc = Document.createDocument("sender", sender)
				.put("url", url)
				.put("roomId", roomId)
				.put("date", date)
				.put("id", id);

		try {
			messages.insert(doc);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean deleteImageMessage(long id)
	{
		Document origin = messages.find(eq("id", id)).firstOrDefault();
		if (origin == null) return false;
		try {
			messages.remove(origin);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public Object[] getMessages(int roomId)
	{
		return new Object[0];
	}
}