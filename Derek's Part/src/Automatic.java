
import StoreMod.Store;
import StoreMod.StoreHelper;
import org.omg.CORBA.ORB;
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
public class Automatic {

    public static void main(String[] args) throws Exception {
        NamingContextExt ncRef = null;
        try {
            // create and initialize the ORB
            ORB orb = ORB.init(args, null);
            // get the root naming context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            // Use NamingContextExt instead of NamingContext. This is part of the Interoperable naming Service.
            ncRef = NamingContextExtHelper.narrow(objRef);

        } catch (Exception e) {
            System.out.println("Error: Server not started, please start servers.");
            System.exit(1);
        }

        Store store1 = StoreHelper.narrow(ncRef.resolve_str("QC Store"));

        Thread t1 = new Thread(() -> {
            System.out.println("QCM0000: (ADD) " + store1.addItem("QCM0000", "QC0001", "tea", 3, 200));
            System.out.println("QCM0000: (ADD) " + store1.addItem("QCM0000", "QC0002", "apple", 2, 200));
            System.out.println("QCM0000: (ADD) " + store1.addItem("QCM0000", "QC0003", "carot", 1, 200));
        });
        Thread t2 = new Thread(() -> {
            System.out.println("ONU1111: (BUY) " + store1.purchaseItem("ONU1111", "QC0001", "10102020"));
            System.out.println("ONU1111: (EXCHANGE) " + store1.exchangeItem("ONU1111", "QC0003", "QC0001", "10102020"));
            System.out.println("ONU1111: (BUY) " + store1.purchaseItem("ONU1111", "QC0002", "10102020"));
        });
        Thread t3 = new Thread(() -> {
            System.out.println("QCU1122: (BUY) " + store1.purchaseItem("QCU1122", "QC0001", "10102020"));
            System.out.println("QCU1122: (BUY) " + store1.purchaseItem("QCU1122", "QC0002", "10102020"));
            System.out.println("QCU1122: (EXCHANGE) " + store1.exchangeItem("QCU1122", "QC0003", "QC0001", "10102020"));
        });
        Thread t4 = new Thread(() -> {
            System.out.println("QCU1133: (BUY) " + store1.purchaseItem("QCU1133", "QC0001", "10102020"));
            System.out.println("QCU1133: (BUY) " + store1.purchaseItem("QCU1133", "QC0002", "10102020"));
        });
        Thread t5 = new Thread(() -> {
            System.out.println("QCU1144: (BUY) " + store1.purchaseItem("QCU1144", "QC0001", "10102020"));
            System.out.println("QCU1144: (BUY) " + store1.purchaseItem("QCU1144", "QC0002", "10102020"));
            System.out.println("QCU1144: (EXCHANGE) " + store1.exchangeItem("QCU1144", "QC0001", "QC0002", "10102020"));
        });
        Thread t6 = new Thread(() -> {
            System.out.println("QCU1155: (BUY) " + store1.purchaseItem("QCU1155", "QC0001", "10102020"));
            System.out.println("QCU1155: (BUY) " + store1.purchaseItem("QCU1155", "QC0002", "10102020"));
        });

        System.out.println("QCM0000: (REMOVE) " + store1.removeItem("QCM0000", "QC0001", -1));
        System.out.println("QCM0000: (REMOVE) " + store1.removeItem("QCM0000", "QC0002", -1));
        System.out.println("QCM0000: (REMOVE) " + store1.removeItem("QCM0000", "QC0003", -1));
        t1.start();
        Thread.sleep(100);
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        t6.start();

    }

}
