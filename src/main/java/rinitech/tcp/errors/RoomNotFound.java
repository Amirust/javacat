package rinitech.tcp.errors;

import rinitech.tcp.packets.MCPPacket;
import rinitech.tcp.packets.json.BaseErrorPackage;
import rinitech.tcp.types.ErrorPacketType;
import rinitech.tcp.types.MajorPacketType;

public class RoomNotFound extends MCPError
{
	public RoomNotFound() { super("Room not found"); }

	public MCPPacket toPacket(String id)
	{
		return new MCPPacket(MajorPacketType.Error, ErrorPacketType.RoomNotFound, new BaseErrorPackage(this.getMessage()), id);
	}
}
