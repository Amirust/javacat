package rinitech.tcp.packets.json;

/**
 * BaseErrorPackage is the base class for all error packets.
 */
public abstract class BaseErrorPackage extends BasePackage
{
	/**
	 * The error message.
	 */
	public String error;

	public BaseErrorPackage(String error)
	{
		this.error = error;
	}
}
