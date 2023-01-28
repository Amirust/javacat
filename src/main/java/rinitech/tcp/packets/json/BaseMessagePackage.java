package rinitech.tcp.packets.json;

import com.google.gson.annotations.SerializedName;
import rinitech.tcp.Room;

public class BaseMessagePackage extends BasePackage
{
	@SerializedName("room")
	public int rawRoom;
	public Room room = Room.fromId(rawRoom);
}
