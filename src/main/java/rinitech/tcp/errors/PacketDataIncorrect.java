package rinitech.tcp.errors;

import rinitech.tcp.packets.MCPPacket;
import rinitech.tcp.packets.json.BaseErrorPackage;
import rinitech.tcp.types.ErrorPacketType;
import rinitech.tcp.types.MajorPacketType;

public class PacketDataIncorrect extends MCPError
{
	public PacketDataIncorrect() { super("Packet data incorrect"); }

	public MCPPacket toPacket()
	{
		return new MCPPacket(MajorPacketType.Error, ErrorPacketType.PacketDataIncorrect, new BaseErrorPackage(this.getMessage()));
	}
}
