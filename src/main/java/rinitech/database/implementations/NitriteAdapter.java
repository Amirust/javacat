package rinitech.database.implementations;

import org.dizitart.no2.Document;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteCollection;
import rinitech.database.DatabaseAdapter;
import rinitech.database.types.Room;
import rinitech.database.types.User;

import static org.dizitart.no2.filters.Filters.eq;

public class NitriteAdapter implements DatabaseAdapter
{
	private final Nitrite db;
	private final NitriteCollection users;
	private final NitriteCollection rooms;
	public NitriteAdapter(String dbPath, String userId, String password)
	{
		db = Nitrite.builder()
			.compressed()
			.filePath(dbPath)
			.openOrCreate(userId, password);

		users = db.getCollection("users");
		rooms = db.getCollection("rooms");
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

	public User getUser(long id)
	{
		Document document = users.find(eq("id", id)).firstOrDefault();
		if (document == null) return null;
		User user = new User();
		user.id = document.get("id", Long.class);
		user.username = document.get("username", String.class);
		user.password = document.get("password", String.class);
		user.avatar = document.get("avatar", String.class);
		return user;
	}

	public boolean addUser(String username, String password)
	{
		Document doc = Document.createDocument("username", username)
				.put("password", password)
				.put("id", users.size() + 1);

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

	public boolean updateUserPassword(long id, String password)
	{
		Document origin = users.find(eq("id", id)).firstOrDefault();
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

	public boolean updateUserAvatar(long id, String avatar)
	{
		Document origin = users.find(eq("id", id)).firstOrDefault();
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

	public boolean updateUserUsername(String username, String newUsername)
	{
		Document origin = users.find(eq("username", username)).firstOrDefault();
		if (origin == null) return false;
		origin.put("username", newUsername);
		try {
			users.update(origin);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean updateUserUsername(long id, String newUsername)
	{
		Document origin = users.find(eq("id", id)).firstOrDefault();
		if (origin == null) return false;
		origin.put("username", newUsername);
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

	public boolean deleteUser(long id)
	{
		Document origin = users.find(eq("id", id)).firstOrDefault();
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

	public void close()
	{
		try {
			users.close();
			rooms.close();
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}