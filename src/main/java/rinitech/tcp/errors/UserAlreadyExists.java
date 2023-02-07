package rinitech.tcp.errors;

import rinitech.tcp.packets.MCPPacket;
import rinitech.tcp.packets.json.BaseErrorPackage;
import rinitech.tcp.types.ErrorPacketType;
import rinitech.tcp.types.MajorPacketType;

public class UserAlreadyExists extends MCPError
{
	public UserAlreadyExists() { super("User already exists"); }

	public MCPPacket toPacket(String id)
	{
		return new MCPPacket(MajorPacketType.Error, ErrorPacketType.UserAlreadyExists, new BaseErrorPackage(this.getMessage()), id);
	}
}
