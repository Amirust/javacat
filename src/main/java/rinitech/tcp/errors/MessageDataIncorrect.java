package rinitech.tcp.errors;

import rinitech.tcp.packets.MCPPacket;
import rinitech.tcp.packets.json.BaseErrorPackage;
import rinitech.tcp.types.ErrorPacketType;
import rinitech.tcp.types.MajorPacketType;

public class MessageDataIncorrect extends MCPError
{
	public MessageDataIncorrect() { super("Message data incorrect"); }

	public MCPPacket toPacket(String id)
	{
		return new MCPPacket(MajorPacketType.Error, ErrorPacketType.MessageDataIncorrect, new BaseErrorPackage(this.getMessage()), id);
	}
}
