package rinitech.tcp.packets.json;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

/**
 * ImageMessage packet sends to the client when the user sends an image to the room.
 * Data contains the image, user and timestamp.
 * @see ImageMessageData
 */
public class ImageMessage extends BaseMessagePackage
{
	public ImageMessageData data;
}

class ImageMessageData
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
	private long rawTime;

	/**
	 * The timestamp of the image.
	 */
	public Date time = new Date(rawTime);
}
