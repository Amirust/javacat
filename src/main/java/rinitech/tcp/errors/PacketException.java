package rinitech.tcp.errors;

import rinitech.tcp.packets.MCPPacket;
import rinitech.tcp.packets.json.BaseErrorPackage;
import rinitech.tcp.types.ErrorPacketType;
import rinitech.tcp.types.MajorPacketType;

public class PacketException extends MCPError
{
	public PacketException(String message)
	{
		super(message);
	}

	public MCPPacket toPacket(String id)
	{
		return new MCPPacket(MajorPacketType.Error, ErrorPacketType.PacketDataIncorrect, new BaseErrorPackage(this.getMessage()), id);
	}
}
