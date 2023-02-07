package rinitech.tcp.errors;

import rinitech.tcp.packets.MCPPacket;
import rinitech.tcp.packets.json.BaseErrorPackage;
import rinitech.tcp.types.ErrorPacketType;
import rinitech.tcp.types.MajorPacketType;

public class UserPasswordInvalid extends MCPError
{
	public UserPasswordInvalid() { super("Invalid password"); }

	public MCPPacket toPacket(String id)
	{
		return new MCPPacket(MajorPacketType.Error, ErrorPacketType.UserPasswordInvalid, new BaseErrorPackage(this.getMessage()), id);
	}
}
