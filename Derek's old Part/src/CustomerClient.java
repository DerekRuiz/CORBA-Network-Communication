
import StoreMod.Store;
import StoreMod.StoreHelper;
import java.rmi.RemoteException;
import java.util.Scanner;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author DRC
 */
public class CustomerClient {

    /**
     * @param args the command line arguments
     * @throws java.rmi.RemoteException
     */
    public static void main(String[] args) throws RemoteException {
        Scanner kb = new Scanner(System.in);
        boolean exitSystem = false;
        NamingContextExt ncRef = null;
        try {
            // create and initialize the ORB
            ORB orb = ORB.init(args, null);
            // get the root naming context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            // Use NamingContextExt instead of NamingContext. This is part of the Interoperable naming Service.
            ncRef = NamingContextExtHelper.narrow(objRef);

        } catch (Exception e) {
            System.out.println("Error: Server not started, pleae start servers.");
            System.exit(1);
        }

        while (!exitSystem) {
            boolean exitOperation = false;
            System.out.print("Welcome to Store\nEnter your customer account ID:\n >");
            String user_id = kb.nextLine().toUpperCase().trim();

            if (user_id.equals("EXIT")) {
                exitSystem = true;
                continue;
            }

            if (user_id.length() < 3 || user_id.charAt(2) != 'U') {
                System.out.println("Entered user id " + user_id + " is not a customer");
                continue;
            }

            // try to resolve the Object Reference in Naming
            Store store = null;
            try {
                store = StoreHelper.narrow(ncRef.resolve_str(user_id.substring(0, 2) + " Store"));
            } catch (NotFound | CannotProceed | InvalidName notFound) {
                System.out.println("Could not obtain a handle on store object: " + user_id.substring(0, 2) + " Store");
                continue;
            }

            while (!exitSystem && !exitOperation) {
                System.out.print("Hello customer " + user_id + ", what operation would you like to do? (Purchase Item, Find Item, Return Item, Exchange Items)\n >");
                String operation = kb.nextLine().trim();
                if (operation.toUpperCase().equals("EXIT")) {
                    exitSystem = true;
                    continue;
                } else if (operation.toUpperCase().equals("BACK")) {
                    exitOperation = true;
                    continue;
                }

                // Customer chose to purchase item
                if (operation.toUpperCase().contains("BUY") || operation.toUpperCase().contains("PURCH")) {
                    System.out.print("Enter the arguments to purchase the item (ItemID, Date of Purchase (ddmmyyyy))\n >");
                    String arguments = kb.nextLine().trim();
                    if (arguments.toUpperCase().equals("EXIT")) {
                        exitSystem = true;
                        continue;
                    } else if (arguments.toUpperCase().equals("BACK")) {
                        continue;
                    }
                    try {
                        String[] argm = arguments.split(",");
                        String result = store.purchaseItem(user_id, argm[0].toUpperCase().trim(), argm[1].trim());
                        if (result.startsWith("WAITLIST")) {
                            System.out.print("Item is unavailable, do you wish to be placed on the waitlist? (y/n)\n >");
                            String input = kb.nextLine().trim();
                            if (input.toUpperCase().contains("Y")) {
                                result = store.purchaseItem(user_id + ":ADD TO WAITLIST", argm[0].toUpperCase().trim(), argm[1].trim());
                            } else {
                                result = "SUCCESS: Not adding to waitlist";
                            }
                        }
                        System.out.println(result.trim());
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        System.out.println("Not enough arguments");
                    }

                    // Customer chose to find item
                } else if (operation.toUpperCase().contains("FIND")) {
                    System.out.print("Enter the argument to find the item (ItemName)\n >");
                    String arguments = kb.nextLine().trim();
                    if (arguments.toUpperCase().equals("EXIT")) {
                        exitSystem = true;
                        continue;
                    } else if (arguments.toUpperCase().equals("BACK")) {
                        continue;
                    }

                    try {
                        String[] argm = arguments.split(",");
                        String[] items = store.findItem(user_id, argm[0].trim());

                        if (items.length == 0) {
                            System.out.println("Search complete: No items found");
                        } else if (items[0].startsWith("ERROR:")) {
                            System.out.println(items[0].trim());
                        } else {
                            System.out.println("Search complete: " + String.join(", ", items).trim());
                        }

                    } catch (ArrayIndexOutOfBoundsException ex) {
                        System.out.println("Not enough arguments");
                    }

                    // Customer chose to return item
                } else if (operation.toUpperCase().contains("RETURN")) {
                    System.out.print("Enter the arguments to return the item (ItemID, Date of Return (ddmmyyyy))\n >");
                    String arguments = kb.nextLine();
                    if (arguments.toUpperCase().equals("EXIT")) {
                        exitSystem = true;
                        continue;
                    } else if (arguments.toUpperCase().equals("BACK")) {
                        continue;
                    }

                    try {
                        String[] argm = arguments.split(",");
                        String result = store.returnItem(user_id, argm[0].toUpperCase().trim(), argm[1].trim());
                        System.out.println(result.trim());
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        System.out.println("Not enough arguments");
                    }
                } else if (operation.toUpperCase().contains("EXCHANGE")) {
                    System.out.print("Enter the arguments to exchange the items (New ItemID, Old ItemID, Date of Exchange (ddmmyyyy))\n >");
                    String arguments = kb.nextLine();
                    if (arguments.toUpperCase().equals("EXIT")) {
                        exitSystem = true;
                        continue;
                    } else if (arguments.toUpperCase().equals("BACK")) {
                        continue;
                    }

                    try {
                        String[] argm = arguments.split(",");
                        String result = store.exchangeItem(user_id, argm[0].toUpperCase().trim(), argm[1].toUpperCase().trim(), argm[2].trim());
                        System.out.println(result.trim());
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        System.out.println("Not enough arguments");
                    }
                } else {
                    System.out.println("Not a valid operation");
                }
            } // close operation while loop
        } // close system while loop
    } // close main

}
