package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

    public static HashMap<Integer, ArrayList<String>> waitingforproposal = new HashMap<Integer, ArrayList<String>>();
    public static HashMap<Integer, ArrayList<message_obj>> purposedkeyslist = new HashMap<Integer, ArrayList<message_obj>>();
    static final int SERVER_PORT = 10000;
    String myPort;
    int proposalno = 1;
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";
    //  /home/sahil/AndroidStudioProjects/GroupMessenger2/app/build/outputs/apk/app-debug.apk
    static ArrayList<String> portnos = new ArrayList<String>(Arrays.asList(REMOTE_PORT0, REMOTE_PORT1, REMOTE_PORT2, REMOTE_PORT3, REMOTE_PORT4));

    static final ReentrantLock lock = new ReentrantLock(true);
    static Comparator<message_obj> comparator = new customcomparator();
    static PriorityQueue<message_obj> queue = new PriorityQueue<message_obj>(40, comparator);
    static int failed = 0;
    static String failport = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        Log.v("Myportno on create", myPort);
        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             *
             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            /*
             * Log is a good way to debug your code. LogCat prints out all the messages that
             * Log class writes.
             *
             * Please read http://developer.android.com/tools/debugging/debugging-projects.html
             * and http://developer.android.com/tools/debugging/debugging-log.html
             * for more information on debugging.
             */
            Log.e("Create Server", "Can't create a ServerSocket");
            //    Log.e("Create Server", e.getMessage());
            e.printStackTrace();
            return;
        }

        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */
        TextView localTextView1 = (TextView) findViewById(R.id.editText1);
        findViewById(R.id.button4).setOnClickListener(
                new OnSendClickListener(tv, localTextView1, getContentResolver(), myPort));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        private static final String TAG = "oye";

        private final ContentResolver serverContentResolver = getContentResolver();
        private final Uri serverUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
        private int count = 0;

        //String[] portnos=new String[]{REMOTE_PORT0,REMOTE_PORT1,REMOTE_PORT2,REMOTE_PORT3,REMOTE_PORT4};

        @Override

        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];

            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */
            //publishProgress("msg");
            Socket socket = null;
            //count=-1;
            while (true) {

                try {

                    Log.v("BEFOREACCCEPT", "FAILUREclientOUT");

                    socket = serverSocket.accept();
                    lock.lock();

                    ObjectInputStream inp = new ObjectInputStream(socket.getInputStream());

                    message_obj msg_received = (message_obj) inp.readObject();
                    String status = msg_received.status;
                    String frmport = msg_received.origin_port;
                    String temps = "";

                    temps = msg_received.message;
                    Log.v("message contentens", temps);
                    Log.v("message status", status);
                    Log.v("message fromport", frmport);
                    Log.v("message contentens", msg_received.toString());
                    inp.close();

                    if (status.equals("new_message")) {
                        if (portnos.contains(msg_received.origin_port)) {
                            Log.v("new_message01", msg_received.message);
                            msg_received.propose(proposalno + "", myPort);
                            queue.add(msg_received);

                            int count = 0;

                            PriorityQueue<message_obj> copy = new PriorityQueue<message_obj>(40, comparator);

                            copy.addAll(queue);
                            Iterator<message_obj> through = copy.iterator();

                            while (through.hasNext()) {

                                message_obj temp = (message_obj) copy.poll();
                                Log.v(count + "", temp.proposed_id + " " + temp.proposed_from_port);
                                count++;
                            }

                            proposalno++;
                            //send back
                            Log.v("msg_received propos", msg_received.proposed_from_port);

                            Socket clsocket = new Socket();
                            Log.v("yocl", "yo");
                            clsocket.connect(new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(frmport)), 15000);
                            Log.v("yoacl", "yoa");
                            try {
                                OutputStream out = clsocket.getOutputStream();

                                ObjectOutputStream oout = new ObjectOutputStream(out);
                                Log.v("sendingnew obj", msg_received.toString());
                                oout.writeObject(msg_received);
                                oout.close();
                                Log.v("reached herer", msg_received.proposed_from_port);
                                clsocket.close();
                            } catch (UnknownHostException e) {
                                Log.e("FAILUREnew_messsage", "ClientTask UnknownHostException");
                                Log.e("FAILedn", " " + frmport);
                                GroupMessengerActivity.handlerfailure(frmport);

                            } catch (IOException e) {
                                Log.e("FAILUREnew", "ClientTask UnknownHostException");
                                Log.e("FAILednio", " " + frmport);
                                GroupMessengerActivity.handlerfailure(frmport);
                            } catch (Exception e) {
                                Log.e("FAILUREexnew1", "New UnknownHostException");
                                Log.e("FAILUREexnew1", " " + frmport);
                                GroupMessengerActivity.handlerfailure(frmport);
                            }

                        }
                    }
                    if (status.equals("proposed_message")) {

                        String pp_frm_port = msg_received.proposed_from_port;

                        String tem = msg_received.message;
                        Log.v("proposal received", "mss " + pp_frm_port + tem + pp_frm_port);
                        //  Log.v("proposal contentens",msg_received.toString());
                        Log.v("prop from porttt1", failed + "");
                        Log.v("prop from portttttttttt", pp_frm_port);
                        int key = msg_received.unique_no;
                        if (waitingforproposal.containsKey(key)) {
                            Log.v("prop from porttttttttt1", "here");
                        } else {
                            Log.v("prop from porttttttttt3", "here");
                            continue;
                        }
                        Log.v("prop from porttttttttt2", "here");
                        ArrayList<String> ss = waitingforproposal.get(key);
                        Log.v("sssize", ss.size() + "");
                        ss.remove(pp_frm_port);
                        String pp_id = msg_received.proposed_id;

                        if (purposedkeyslist.containsKey(key)) {
                            Log.v("sssize1", "1");
                            ArrayList<message_obj> ps = purposedkeyslist.get(key);
                            ps.add(msg_received);
                            purposedkeyslist.put(key, ps);
                        } else {
                            Log.v("sssize2", "2");
                            ArrayList<message_obj> firstentry_prop = new ArrayList<message_obj>();
                            firstentry_prop.add(msg_received);
                            purposedkeyslist.put(key, firstentry_prop);
                        }

                        if (ss.size() == 1 && failed == 0) {
                            Log.v("ssize3", "3");
                            String lasttocheck = ss.get(0);
                            //// check it if it has failed last may be
                            message_obj msg_objl = new message_obj(50, myPort, "temp");
                            msg_objl.status = "ping";
                            Socket tssocket3 = new Socket();
                            try {
                                tssocket3.connect(new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(lasttocheck)), 15000);

                                OutputStream tsdelout = tssocket3.getOutputStream();

                                ObjectOutputStream tsdeloout3 = new ObjectOutputStream(tsdelout);
                                tsdeloout3.writeObject(msg_objl);

                                tssocket3.close();
                            } catch (IOException e) {
                                Log.v("sahil92", "sahil921");
                                GroupMessengerActivity.handlerfailure(lasttocheck);
                            } catch (Exception e) {
                                Log.v("sahil93", "sahil932");

                            }

                        }

                        if (ss.size() == 0) {

                            Log.v("confirmef drom all", "yeeee");
                            Log.v("confirmallport", msg_received.message);
                            ArrayList<message_obj> psp = purposedkeyslist.get(key);
                            Collections.sort(psp, new customcomparator());
                            for (message_obj temp : psp) {
                                Log.v("sahil" + "", temp.proposed_id + " " + temp.proposed_from_port);
                            }
                            message_obj tobedelivered = psp.get(psp.size() - 1);
                            tobedelivered.delivery();

                            purposedkeyslist.remove(key);
                            waitingforproposal.remove(key);

                            ///////delivery to all
                            Log.v("messagetobedel00", tobedelivered.message);
                            ArrayList<String> ts = new ArrayList<String>();
                            ts = (ArrayList<String>) portnos.clone();
                            for (String eachport : portnos) {
                                Log.v("messagetobedel05", "portno: " + eachport + " " + tobedelivered.message);
                                Log.v("sahil27eachport", "" + eachport);
                                ts.remove(eachport);
                                Log.v("sahil2", "here0");
                                Socket delsocket = new Socket();
                                Log.v("sahil2", "here20");
                                delsocket.connect(new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(eachport)), 15000);
                                Log.v("sahil2", "here30");

                                try {
                                    OutputStream delout = delsocket.getOutputStream();
                                    Log.v("sahil2", "here40");

                                    ObjectOutputStream deloout = new ObjectOutputStream(delout);
                                    Log.v("sahil2", "here5000");
                                    Log.v("sending4 delivery obj", tobedelivered.toString());
                                    deloout.writeObject(tobedelivered);

                                    Log.v("delout48", "delout.toString()");
                                    /*
                                     * TODO: Fill in your client code that sends out a message.
                                     */

                                    delsocket.close();

                                } catch (UnknownHostException e) {
                                    Log.e("FAILUREproposed_message", "ClientTask UnknownHostException");
                                    Log.e("FAILedp", " " + eachport);
                                    GroupMessengerActivity.handlerfailure(eachport);

                                } catch (IOException e) {
                                    Log.e("FAILUREnew", "ClientTask UnknownHostException");
                                    Log.e("FAILedpio", " " + eachport);
                                    GroupMessengerActivity.handlerfailure(eachport);
                                    //////// remaining
                                    Log.v("messagetobedel01", tobedelivered.message);
                                    for (String tseach : ts) {
                                        Log.v("messagetobedel04", "portno: " + tseach + " " + tobedelivered.message);
                                        Log.v("sahil22", "here00");
                                        Socket tssocket = new Socket();
                                        Log.v("sahil22", "here200");
                                        tssocket.connect(new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(tseach)), 15000);
                                        Log.v("sahil22", "here300");

                                        OutputStream tsdelout = tssocket.getOutputStream();
                                        Log.v("sahil2", "here40");

                                        ObjectOutputStream tsdeloout = new ObjectOutputStream(tsdelout);
                                        Log.v("sahil2", "here50");
                                        Log.v("sending3 delivery obj", tobedelivered.toString());
                                        tsdeloout.writeObject(tobedelivered);

                                        Log.v("delout49", tsdeloout.toString());
                                        /*
                                         * TODO: Fill in your client code that sends out a message.
                                         */
                                        tssocket.close();

                                    }
                                    purposedkeyslist.remove(key);
                                    waitingforproposal.remove(key);
                                    //////
                                    //continue;
                                    break;
                                } catch (Exception e) {
                                    Log.e("DeliverysendFAILURE", "Exception");
                                    Log.e("FAILedpio", " " + eachport);
                                    GroupMessengerActivity.handlerfailure(eachport);
                                    continue;
                                }

                            }

                            ////////
                            Log.v("proposedserver keys", psp.toString());

                            purposedkeyslist.remove(key);
                            waitingforproposal.remove(key);

                        } else {
                            Log.v("confirmef remaining", "" + ss.size());
                            waitingforproposal.put(key, ss);
                        }

                    }
                    if (status.equals("delivery_message")) {
                        try {
                            Thread.sleep(500);                 //1000 milliseconds is one second.
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                        //replace with already in queue with status proposal
                        Log.v("del010101", "00 " + msg_received.message);
                        queue.remove(msg_received);//removed old proposal message as compared with unique no from overide equal func

                        queue.add(msg_received);
//                    publishProgress(temps);
                        Log.v("print after ddelivery", "print final");
                        int count = 0;

                        PriorityQueue<message_obj> copy = new PriorityQueue<message_obj>(40, comparator);;
                        copy.addAll(queue);
                        Iterator<message_obj> through = copy.iterator();
                        Log.v("del01", "000000000 " + failport);
                        while (through.hasNext()) {

                            message_obj temp = (message_obj) copy.poll();
                            Log.v(count + "del01", temp.proposed_id + " origin:" + temp.origin_port + " " + temp.proposed_from_port + temp.status + temp.unique_no + "mess:" + temp.message);
                            count++;
                        }

                        //check queue heads for delivery
                        Iterator<message_obj> queueitr = queue.iterator();
                        while (queueitr.hasNext()) {

                            message_obj temp = (message_obj) queue.peek();
                            Log.v("delele from queue", temp.proposed_id + " " + temp.proposed_from_port);
                            Log.v("delelestat0", temp.status + " ");
                            Log.v("delelestat0.5", temp.origin_port + " ");
                            if (temp.status.equals("delivery_message")) {
                                publishProgress(temp.message);
                                queue.remove();
                                Log.v("queuesizedel1", queue.size() + "");
                            } else if (portnos.contains(temp.origin_port)) {
                                break;
                            } else {
                                queue.remove();
                                Log.v("delelestat3", queue.size() + "");

                            }
                        }
                    }

                    if (failed < 50 && failed != 0) {
                        portnos.remove(failport);
                        failed++;
                        ////
                        String failedport = failport;
                        try {
                            Log.v("Handlefailure", "I will handle failure");
                            Log.v("Handlefailureport", failedport);
                            //portnos.remove(failedport);

                            Log.v("queuesizedel2", queue.size() + "");

                            ///changes dummy message
                            for (Integer key : waitingforproposal.keySet()) {

                                ArrayList<String> www = (ArrayList<String>) waitingforproposal.get(key);
                                if (www.contains(failport)) {
                                    //www.remove(failport);

                                    message_obj dummym = new message_obj(key, myPort, "dummy");
                                    dummym.propose("0", failport);
                                    Socket sockettemp45 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(myPort));
                                    OutputStream outtemp45 = sockettemp45.getOutputStream();

                                    ObjectOutputStream oouttemp45 = new ObjectOutputStream(outtemp45);
                                    oouttemp45.writeObject(dummym);
                                    sockettemp45.close();

                                    Log.v("dummy", "dummykey" + key);

                                }
                            }

                        
                        } catch (NullPointerException e) {
                            Log.v("failurepointouter1", "nullpoint");
                        } catch (Exception e) {
                            Log.v("failurepointouter12", "nullpoint");
                        }

                        ///
                    }

                    Log.v("serverendeach", "yo");
                } catch (SocketTimeoutException e) {
                    //  Log.v("servertasktry1",e.getMessage());
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    Log.e("servertasktry2", "ClientTask UnknownHostException");
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.e("servertasktry3", "ClientTask socket IOException");
                //Log.e("servertasktry4", e.getMessage());
                    //e.printStackTrace();
                    continue;

                } catch (Exception e) {
                    //    Log.e("servertasktry5", e.getMessage());
                    e.printStackTrace();
                    continue;
                } finally {
                    lock.unlock();
                }
            }

        }

        protected void onProgressUpdate(String... strings) {
            /*
             * The following code displays what is received in doInBackground().
             */

            Log.v("ser_check_prog-1", "tep");

            try {
                //Log.v("ser_check_prog1","trystatt");
                String strReceived = strings[0].trim();
                Log.v("ser_check_prog0", strReceived);
                String counter = "" + count;
                Log.v("ser_check_prog1", strReceived);
                Log.v("ser_check_prog2", counter);

                ContentValues cv = new ContentValues();
                cv.put("key", counter);
                cv.put("value", strReceived);
                Log.v("ser_check_prog2", cv.toString());

                serverContentResolver.insert(serverUri, cv);
                count++;
                Log.v("stringrec", strReceived);
                TextView remoteTextView = (TextView) findViewById(R.id.textView1);
                remoteTextView.append("key " + counter + " " + strReceived + "\t\n");

            //TextView localTextView = (TextView) findViewById(R.id.textView1);
                //localTextView.append("\n");

                /*
                 * The following code creates a file in the AVD's internal storage and stores a file.
                 *
                 * For more information on file I/O on Android, please take a look at
                 * http://developer.android.com/training/basics/data-storage/files.html
                 */
                return;
            } catch (Exception e) {
                Log.e("serveron progress", "sometemp");
                //     Log.e("serveron progress",e.getMessage());
                e.printStackTrace();
            }
        }

        private Uri buildUri(String scheme, String authority) {
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority(authority);
            uriBuilder.scheme(scheme);
            return uriBuilder.build();
        }
    }

    static void handlerfailure(String failedport) {

        if (failed == 0) {
            ArrayList<String> newports = new ArrayList<String>();
            newports = (ArrayList<String>) portnos.clone();
            newports.remove(failedport);
            failed = 1;
            failport = failedport;
            Log.v("failure function", failedport + "");

        }
    }

}
