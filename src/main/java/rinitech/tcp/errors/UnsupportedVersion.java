package rinitech.tcp.errors;

import rinitech.tcp.packets.MCPPacket;
import rinitech.tcp.packets.json.BaseErrorPackage;
import rinitech.tcp.types.ErrorPacketType;
import rinitech.tcp.types.MajorPacketType;

public class UnsupportedVersion extends MCPError
{
	public UnsupportedVersion() { super("Unsupported version"); }

	public MCPPacket toPacket(String id)
	{
		return new MCPPacket(MajorPacketType.Error, ErrorPacketType.UnsupportedVersion, new BaseErrorPackage(this.getMessage()), id);
	}
}
