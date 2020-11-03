
import StoreMod.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;

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

        try {
            // create and initialize the ORB
            ORB orb = ORB.init(args, null);
            // get reference to rootpoa & activate the POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();
            // create servant and register it with the ORB
            StoreServer s1 = new StoreServer("QC", QCport);
            s1.setORB(orb);
            // get object reference from the servant
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(s1);
            Store href = StoreHelper.narrow(ref);
            // get the root naming context
            // NameService invokes the name service
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            // Use NamingContextExt which is part of the Interoperable Naming Service (INS) specification.
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            // bind the Object Reference in Naming
            String name = "QC Store";
            NameComponent[] path = ncRef.to_name(name);
            ncRef.rebind(path, href);

            StoreServer s2 = new StoreServer("ON", ONport);
            s2.setORB(orb);
            ref = rootpoa.servant_to_reference(s2);
            href = StoreHelper.narrow(ref);
            name = "ON Store";
            path = ncRef.to_name(name);
            ncRef.rebind(path, href);

            StoreServer s3 = new StoreServer("BC", BCport);
            s3.setORB(orb);
            ref = rootpoa.servant_to_reference(s3);
            href = StoreHelper.narrow(ref);
            name = "BC Store";
            path = ncRef.to_name(name);
            ncRef.rebind(path, href);

            System.out.println("Servers ready and waiting ...");
            // wait for invocations from clients
            orb.run();
        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }
        System.out.println("HelloServer Exiting ...");

    }

}
