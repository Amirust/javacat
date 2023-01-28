package rinitech.tcp.packets.json;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

/**
 * TextMessage packet sends to the client when the user sends a text message to the room.
 * Data contains the message, user and timestamp.
 * @see TextMessageData
 */
public class TextMessage extends BaseMessagePackage
{
	public TextMessageData data;
}

class TextMessageData
{
	/**
	 * The message.
	 */
	public String message;
	/**
	 * The username who sent the message.
	 */
	public String user;

	/**
	 * The raw timestamp of the message.
	 */
	@SerializedName("time")
	private long rawTime;

	/**
	 * The timestamp of the message.
	 */
	public Date time = new Date(rawTime);
}
