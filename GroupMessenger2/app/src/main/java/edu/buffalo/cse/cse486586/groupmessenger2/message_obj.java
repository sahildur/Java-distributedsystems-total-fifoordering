package edu.buffalo.cse.cse486586.groupmessenger2;

import java.io.Serializable;

/**
 * Created by sahil on 2/27/16.
 */
public class message_obj implements Serializable {

    int unique_no = 0;
    String origin_port = "";
    String status = "";//new message, proposed_message,delivery_message
    String proposed_id = "";
    String proposed_from_port = "";

    String message = "";

    public message_obj(int un, String port, String msg) {
        unique_no = un;
        origin_port = port;
        message = msg;
        status = "new_message";

    }

    public void propose(String ppid, String pp_fp) {
        proposed_id = ppid;
        proposed_from_port = pp_fp;
        status = "proposed_message";
    }

    public void delivery() {

        status = "delivery_message";
    }

    public boolean equals(Object o) {
        if (o instanceof message_obj) {
            message_obj c = (message_obj) o;
            return unique_no == c.unique_no && origin_port.equals(c.origin_port);
        }
        return false;
    }

}
