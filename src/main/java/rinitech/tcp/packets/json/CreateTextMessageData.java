package rinitech.tcp.packets.json;

import com.google.gson.annotations.SerializedName;

public class CreateTextMessageData
{
	/**
	 * The message.
	 */
	@SerializedName("text")
	public String text;
}
