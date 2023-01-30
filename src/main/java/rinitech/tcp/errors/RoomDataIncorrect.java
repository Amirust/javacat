package rinitech.tcp.errors;

import rinitech.tcp.packets.MCPPacket;
import rinitech.tcp.packets.json.BaseErrorPackage;
import rinitech.tcp.types.ErrorPacketType;
import rinitech.tcp.types.MajorPacketType;

public class RoomDataIncorrect extends MCPError
{
	public RoomDataIncorrect() { super("Room data incorrect"); }

	public MCPPacket toPacket()
	{
		return new MCPPacket(MajorPacketType.Error, ErrorPacketType.RoomDataIncorrect, new BaseErrorPackage(this.getMessage()));
	}
}
