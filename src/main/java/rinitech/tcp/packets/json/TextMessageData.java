package rinitech.tcp.packets.json;

import com.google.gson.annotations.SerializedName;

public class TextMessageData
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
	public long rawTime;
}
