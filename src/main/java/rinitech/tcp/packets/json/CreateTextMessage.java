package rinitech.tcp.packets.json;


/**
 * CreateTextMessage packet sends to the server when the client wants to create a new text message.
 * Data contains the message.
 * @see CreateTextMessageData
 */
public class CreateTextMessage extends BaseMessagePackage
{
	public CreateTextMessageData data;
}

class CreateTextMessageData
{
	/**
	 * The message.
	 */
	public String message;
}