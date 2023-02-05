package rinitech;

import rinitech.config.Config;
import rinitech.config.Parser;
import rinitech.database.DatabaseAdapter;
import rinitech.database.implementations.NitriteAdapter;
import rinitech.tcp.Server;

import java.io.IOException;
import java.util.Objects;

public class Main
{
	public static void main(String[] args)
	{
		Config config = new Config();
		config.http = Parser.parse("miricat.http");
		config.port = Integer.parseInt(Objects.requireNonNull(Parser.parse("miricat.port")));
		config.rootUsername = Parser.parse("root.username");
		config.rootPassword = Parser.parse("root.password");
		config.dbpath = Parser.parse("db.path");
		config.dbUser = Parser.parse("db.user");
		config.dbPass = Parser.parse("db.password");

		DatabaseAdapter db = new NitriteAdapter(config.dbpath, config.dbUser, config.dbPass);
		try {
			Server server = new Server(config, db,config.port);
			Runtime.getRuntime().addShutdownHook(new Thread(() ->
			{
				System.out.println("Shutting down server...");
				try {
					server.stop();
					db.close();
				} catch (IOException | InterruptedException ignored) {}
				System.out.println("Server stopped.\n");
				System.exit(0);
			}));
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}