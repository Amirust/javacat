package rinitech.tcp.errors;

import rinitech.tcp.packets.MCPPacket;
import rinitech.tcp.packets.json.BaseErrorPackage;
import rinitech.tcp.types.ErrorPacketType;
import rinitech.tcp.types.MajorPacketType;

public class UserNotFound extends MCPError
{
	public UserNotFound() { super("User not found"); }

	public MCPPacket toPacket(String id)
	{
		return new MCPPacket(MajorPacketType.Error, ErrorPacketType.UserNotFound, new BaseErrorPackage(this.getMessage()), id);
	}
}
