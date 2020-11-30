package store;


/**
 *
 * @author DRC
 */
public class Manager {

    String manager_id;

    public Manager(String manager_id) {
        this.manager_id = manager_id;
    }

    public boolean equals(Manager obj) {
        return this.manager_id.equals(obj.manager_id);
    }

}
