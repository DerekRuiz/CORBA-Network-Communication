
import StoreMod.*;
import org.omg.CORBA.*;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author DRC
 */
public class ShutdownServers {

    public static void main(String[] args) {
        try {
            // create and initialize the ORB
            ORB orb = ORB.init(args, null);
            // get the root naming context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            // Use NamingContextExt instead of NamingContext. This is part of the Interoperable naming Service.
             NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            // try to resolve the Object Reference in Naming
            Store store = StoreHelper.narrow(ncRef.resolve_str("QC Store"));
            store.shutdown();
            

        } catch (Exception e) {
            System.out.println("Error: "+ e);
            System.exit(1);
        }
    }

}
