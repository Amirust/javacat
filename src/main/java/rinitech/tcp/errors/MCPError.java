package rinitech.tcp.errors;

import org.apache.commons.lang3.NotImplementedException;
import rinitech.tcp.packets.MCPPacket;

public abstract class MCPError extends Error
{
	public MCPError(String message)
	{
		super(message);
	}

	@Override
	public String toString()
	{
		return super.toString();
	}

	public abstract MCPPacket toPacket();
}
