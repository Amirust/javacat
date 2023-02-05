package rinitech.tcp.packets.json;

import com.google.gson.annotations.*;

public abstract class BaseMessagePackage extends BasePackage
{
	@SerializedName("room")
	public int rawRoom;
}
