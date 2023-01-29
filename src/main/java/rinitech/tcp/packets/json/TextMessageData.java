package rinitech.tcp.packets.json;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

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
	private long rawTime;

	/**
	 * The timestamp of the message.
	 */
	public Date time = new Date(rawTime);
}
