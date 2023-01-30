package rinitech.tcp.errors;

import rinitech.tcp.packets.MCPPacket;
import rinitech.tcp.packets.json.BaseErrorPackage;
import rinitech.tcp.types.ErrorPacketType;
import rinitech.tcp.types.MajorPacketType;

public class RoomAlreadyExists extends MCPError
{
	public RoomAlreadyExists() { super("Room already exists"); }

	public MCPPacket toPacket()
	{
		return new MCPPacket(MajorPacketType.Error, ErrorPacketType.RoomAlreadyExists, new BaseErrorPackage(this.getMessage()));
	}
}
