package rinitech.tcp.packets.json;

/**
 * CreateImageMessage packet sends to the server when the client wants to send image to room.
 * Data contains the image url.
 * @see CreateImageMessageData
 */
public class CreateImageMessage extends BaseMessagePackage
{
	public CreateImageMessageData data;
}

