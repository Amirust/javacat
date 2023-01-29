package rinitech.tcp.packets.json;

/**
 * TextMessage packet sends to the client when the user sends a text message to the room.
 * Data contains the message, user and timestamp.
 * @see TextMessageData
 */
public class TextMessage extends BaseMessagePackage
{
	public TextMessageData data;
}

