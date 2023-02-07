package rinitech.tcp.errors;

import rinitech.tcp.packets.MCPPacket;
import rinitech.tcp.packets.json.BaseErrorPackage;
import rinitech.tcp.types.ErrorPacketType;
import rinitech.tcp.types.MajorPacketType;

public class AuthDataIncorrect extends MCPError
{
	public AuthDataIncorrect() { super("Auth data incorrect"); }

	public MCPPacket toPacket(String id)
	{
		return new MCPPacket(MajorPacketType.Error, ErrorPacketType.AuthDataIncorrect, new BaseErrorPackage(this.getMessage()), id);
	}
}
