
import java.util.Arrays;
import java.util.GregorianCalendar;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author DRC
 */
public class test {

    public static void main(String[] args) throws Exception {
        
        String s = null;
        s.toString();
        
        String dateOfPurchase = "14102020";
        String dateOfReturn = "15112020";
        GregorianCalendar a = new GregorianCalendar(2020, 0, 1);
        

        GregorianCalendar expired = new GregorianCalendar(Integer.valueOf(dateOfPurchase.substring(4, 8)), Integer.valueOf(dateOfPurchase.substring(2, 4)) - 1, Integer.valueOf(dateOfPurchase.substring(0, 2)));
        expired.add(GregorianCalendar.DAY_OF_MONTH, 30);
        GregorianCalendar returned = new GregorianCalendar(Integer.valueOf(dateOfReturn.substring(4, 8)), Integer.valueOf(dateOfReturn.substring(2, 4)) - 1, Integer.valueOf(dateOfReturn.substring(0, 2)));
        
        System.out.println("expired: " + expired.getTime());
        System.out.println("returned: " + returned.getTime());
        if (returned.before(expired)) {
            System.out.println("before");
        } else {
            System.out.println("after");
        }
    }

}
