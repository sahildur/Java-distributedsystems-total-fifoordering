package edu.buffalo.cse.cse486586.groupmessenger2;

import android.util.Log;

import java.util.Comparator;

/**
 * Created by sahil on 2/28/16.
 */
public class customcomparator implements Comparator<message_obj>
{
    public int compare(message_obj x, message_obj y)
    {
        if(Integer.parseInt(x.proposed_id)>Integer.parseInt(y.proposed_id))
        {
            return 1;
        }
        else if(Integer.parseInt(x.proposed_id)<Integer.parseInt(y.proposed_id))
        {
            return -1;
        }else{

        if(Integer.parseInt(x.proposed_from_port)>Integer.parseInt(y.proposed_from_port))
        {
            return 1;
        }
        else if(Integer.parseInt(x.proposed_from_port)<Integer.parseInt(y.proposed_from_port)) {
            return -1;
        }
        return 0;
        }

    }


}