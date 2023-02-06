package rinitech.tcp.errors;

import rinitech.tcp.packets.MCPPacket;
import rinitech.tcp.packets.json.BaseErrorPackage;
import rinitech.tcp.types.ErrorPacketType;
import rinitech.tcp.types.MajorPacketType;

public class HeartbeatTimeout extends MCPError
{
	public HeartbeatTimeout() { super("Heartbeat Timeout"); }

	public MCPPacket toPacket()
	{
		return new MCPPacket(MajorPacketType.Error, ErrorPacketType.HeartbeatTimeout, new BaseErrorPackage(this.getMessage()));
	}
}
