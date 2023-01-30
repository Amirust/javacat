package rinitech;

import rinitech.config.Config;
import rinitech.config.Parser;
import rinitech.database.DatabaseAdapter;
import rinitech.database.implementations.NitriteAdapter;
import rinitech.tcp.Server;

public class Main
{
	public static void main(String[] args)
	{
		Config config = new Config();
		config.http = Parser.parse("http");
		System.out.println("HTTP: " + config.http);
		DatabaseAdapter db = new NitriteAdapter("U:\\Projects\\javacat\\nitrite.db", "nitrite", "dontmatterlol");
		try {
			Server server = new Server(config, db,3072);
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}