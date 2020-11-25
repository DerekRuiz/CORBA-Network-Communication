package common;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import replica_manager.ReplicaManager;

public class RMDriver {

	public static void main(String[] args) throws UnknownHostException, FileNotFoundException {
		
		 Scanner fScn = new Scanner(new File("addresses.txt"));
	     String data;
	    

	       data = fScn.nextLine();
	
	       String[] token = data.split(",");
	
	       InetAddress RM1Address = InetAddress.getByName(token[4]);
	       int RM1Port = Integer.parseInt(token[5]);
	       
	       InetAddress RM2Address = InetAddress.getByName(token[6]);
	       int RM2Port = Integer.parseInt(token[7]);
	       
	       InetAddress RM3Address = InetAddress.getByName(token[8]);
	       int RM3Port = Integer.parseInt(token[9]);
	      
	      fScn.close();
		
		ReplicaManager rm1 = new ReplicaManager(RM1Address, RM1Port, false, true);
		ReplicaManager rm2 = new ReplicaManager(RM2Address, RM2Port, false, false);
		ReplicaManager rm3 = new ReplicaManager(RM3Address, RM3Port, false, false);

	}
	
	
	private void ReadAddresses() throws UnknownHostException, FileNotFoundException {
		
	}
	
}
