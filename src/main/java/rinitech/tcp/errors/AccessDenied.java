package rinitech.tcp.errors;

import rinitech.tcp.packets.MCPPacket;
import rinitech.tcp.packets.json.BaseErrorPackage;
import rinitech.tcp.types.ErrorPacketType;
import rinitech.tcp.types.MajorPacketType;

public class AccessDenied extends MCPError
{
	public AccessDenied() { super("Access denied"); }

	public MCPPacket toPacket()
	{
		return new MCPPacket(MajorPacketType.Error, ErrorPacketType.AccessDenied, new BaseErrorPackage(this.getMessage()));
	}
}
