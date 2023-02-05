package rinitech.tcp.packets.json;

import com.google.gson.annotations.SerializedName;

public class ImageMessageData
{
	/**
	 * The image url.
	 */
	public String image;
	/**
	 * The username who sent the image.
	 */
	public String user;

	/**
	 * The raw timestamp of the image.
	 */
	@SerializedName("time")
	public long rawTime;
}
