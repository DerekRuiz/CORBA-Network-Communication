
import StoreMod.Store;
import StoreMod.StoreHelper;
import java.util.Scanner;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author DRC
 */
public class ManagerClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
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
            System.out.println("Error: Server not started, please start servers.");
            System.exit(1);
        }

        while (!exitSystem) {
            boolean exitOperation = false;
            System.out.print("Welcome to Store\nEnter your manager account ID:\n >");
            String user_id = kb.nextLine().toUpperCase().trim();

            if (user_id.toUpperCase().equals("EXIT")) {
                exitSystem = true;
                continue;
            }

            if (user_id.length() < 3 || user_id.charAt(2) != 'M') {
                System.out.println("Entered user id " + user_id + " is not a manager");
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
                System.out.print("Hello manager " + user_id + ", what operation would you like to do? (Add Item, Remove Item, List Item)\n >");
                String operation = kb.nextLine().trim();
                if (operation.toUpperCase().equals("EXIT")) {
                    exitSystem = true;
                    continue;
                } else if (operation.toUpperCase().equals("BACK")) {
                    exitOperation = true;
                    continue;
                }

                // Manager chose to add item
                if (operation.toUpperCase().contains("ADD")) {
                    System.out.print("Enter the arguments to add the item (ItemID, Item Name, Quantity, Price)\n >");
                    String arguments = kb.nextLine().trim();
                    if (arguments.toUpperCase().equals("EXIT")) {
                        exitSystem = true;
                        continue;
                    } else if (arguments.toUpperCase().equals("BACK")) {
                        continue;
                    }

                    try {
                        String[] argm = arguments.split(",");
                        int quantity = Integer.parseInt(argm[2].trim());
                        double price = Double.parseDouble(argm[3].trim());

                        String result = store.addItem(user_id, argm[0].toUpperCase().trim(), argm[1].trim(), quantity, price);
                        System.out.println(result);
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        System.out.println("Not enough arguments");
                    } catch (NumberFormatException ex) {
                        System.out.println("Quantity or Price not in number format");
                    }

                    // Manager chose to remove item
                } else if (operation.toUpperCase().contains("REMOVE")) {
                    System.out.print("Enter the argument to remove the item (ItemID, Quantity)\n >");
                    String arguments = kb.nextLine().trim();
                    if (arguments.toUpperCase().equals("EXIT")) {
                        exitSystem = true;
                        continue;
                    } else if (arguments.toUpperCase().equals("BACK")) {
                        continue;
                    }

                    try {
                        String[] argm = arguments.split(",");
                        int quantity = Integer.parseInt(argm[1].trim());
                        String result = store.removeItem(user_id, argm[0].toUpperCase().trim(), quantity);
                        System.out.println(result);
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        System.out.println("Not enough arguments");
                    } catch (NumberFormatException ex) {
                        System.out.println("Quantity not in number format");
                    }

                    // Manager chose to list item
                } else if (operation.toUpperCase().contains("LIST")) {
                    String[] items = store.listItemAvailability(user_id);
                    if (items.length == 0) {
                        System.out.print("Listing complete: No items in store\n >");
                    } else if (items[0].startsWith("ERROR:")) {
                        System.out.println(items[0]);
                    } else {
                        System.out.println("Listing complete: " + String.join(", ", items));
                    }
                } else {
                    System.out.println("Not a valid operation");
                }
            } // close operation while loop  

        } // close system while loop
    } // close main

}
