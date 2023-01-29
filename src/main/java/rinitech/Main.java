package rinitech;

import rinitech.tcp.Server;

public class Main
{
	public static void main(String[] args)
	{
		try {
			Server server = new Server(3072);
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}