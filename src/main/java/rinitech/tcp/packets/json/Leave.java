package rinitech.tcp.packets.json;

/**
 * Leave packet sends to the server when the client wants to leave a room.
 * Data contains the id of the room.
 * @see LeaveData
 */
public class Leave extends BasePackage
{
	public LeaveData data;
}

