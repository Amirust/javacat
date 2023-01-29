package rinitech.tcp.packets.json;

/**
 * ImageMessage packet sends to the client when the user sends an image to the room.
 * Data contains the image, user and timestamp.
 * @see ImageMessageData
 */
public class ImageMessage extends BaseMessagePackage
{
	public ImageMessageData data;
}

