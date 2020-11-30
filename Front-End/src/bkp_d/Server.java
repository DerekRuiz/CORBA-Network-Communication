package store;

import java.util.Scanner;
import javax.xml.ws.Endpoint;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author DRC
 */
public class Server {

    static final int QCport = 6789;
    static final int ONport = 6889;
    static final int BCport = 6989;
    static final int ClientPort = 7089;

    public static void main(String[] args) throws Exception {
        Scanner kb = new Scanner(System.in);

        StoreServer s1 = new StoreServer("QC", QCport);
        String name = "QC_Store";
        Endpoint endpoint1 = Endpoint.publish("http://localhost:8080/" + name, s1);

        StoreServer s2 = new StoreServer("ON", ONport);
        name = "ON_Store";
        Endpoint endpoint2 = Endpoint.publish("http://localhost:8080/" + name, s2);
        
        StoreServer s3 = new StoreServer("BC", BCport);
        name = "BC_Store";
        Endpoint endpoint3 = Endpoint.publish("http://localhost:8080/" + name, s3);

        System.out.println("Servers ready and waiting ...");
        System.out.println("Press enter to exit");
        kb.nextLine();
        endpoint1.stop();
        endpoint2.stop();
        endpoint3.stop();
        kb.close();
        System.out.println("Exited");
        
    }

}
