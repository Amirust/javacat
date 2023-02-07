package rinitech.tcp.errors;

import rinitech.tcp.packets.MCPPacket;
import rinitech.tcp.packets.json.BaseErrorPackage;
import rinitech.tcp.types.ErrorPacketType;
import rinitech.tcp.types.MajorPacketType;

public class RoomDontExists extends MCPError
{
	public RoomDontExists() { super("Room don't exists"); }

	public MCPPacket toPacket(String id)
	{
		return new MCPPacket(MajorPacketType.Error, ErrorPacketType.RoomDontExists, new BaseErrorPackage(this.getMessage()), id);
	}
}
