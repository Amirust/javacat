package rinitech.tcp.packets.json;

/**
 * Login packet sends to the server when the client wants to log in.
 * Data contains the username and password.
 * @see LoginData
 */
public class Login extends BasePackage
{
	public LoginData data;
}

class LoginData
{
	/**
	 * The username of the user.
	 */
	public String username;
	/**
	 * The password of the user.
	 */
	public String password;
}