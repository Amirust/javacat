package rinitech.tcp.packets.json;

/**
 * Accepted packet sends to the client when the client has successfully authorized.
 * Data contains the http address of the server and the rooms the client is in.
 * @see AcceptedData
 */
public class Accepted extends BasePackage
{
	public AcceptedData data;
}