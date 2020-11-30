package common;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import replica_manager.ReplicaManager;

public class RMDriver {

	public static void main(String[] args) throws UnknownHostException, FileNotFoundException {
		
		if (args.length == 4) {
			System.out.println(args[0]);
			System.out.println(args[1]);
			
			InetAddress RMAddress = InetAddress.getByName(args[0]);
			int port = Integer.parseInt(args[1]);
			boolean failMode = Boolean.parseBoolean(args[2]);
			boolean crashMode = Boolean.parseBoolean(args[3]);
			System.out.println(failMode);
			System.out.println(crashMode);
		      
			ReplicaManager rm = new ReplicaManager(RMAddress, port, failMode, crashMode);
			System.out.println("Created RM");
		}
		else
			System.out.println("Missing arguments");
	}
}
