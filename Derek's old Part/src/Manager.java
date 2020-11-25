/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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
