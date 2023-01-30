package rinitech.tcp.errors;

import rinitech.tcp.packets.MCPPacket;
import rinitech.tcp.packets.json.BaseErrorPackage;
import rinitech.tcp.types.ErrorPacketType;
import rinitech.tcp.types.MajorPacketType;

public class UserPasswordRequired extends MCPError
{
	public UserPasswordRequired()
	{
		super("User password required");
	}

	public MCPPacket toPacket()
	{
		return new MCPPacket(MajorPacketType.Error, ErrorPacketType.UserPasswordRequired, new BaseErrorPackage(this.getMessage()));
	}
}
