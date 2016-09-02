package edu.buffalo.cse.cse486586.groupmessenger2;

import android.content.ContentResolver;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by sahil on 2/26/16.
 */
public class OnSendClickListener implements View.OnClickListener {

    private final TextView mTextView;
    private final TextView localeditViewsend;
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";
    ArrayList<String> portnos=new ArrayList<String>(Arrays.asList(REMOTE_PORT0, REMOTE_PORT1, REMOTE_PORT2, REMOTE_PORT3, REMOTE_PORT4));
    public int uniqueno=0;
    private final ContentResolver sendContentResolver;
    String myPort;


    public OnSendClickListener(TextView _tv,TextView _editlocal,ContentResolver _cr,String myownport ) {
        mTextView=_tv;
        localeditViewsend=_editlocal;
        sendContentResolver = _cr;
        myPort=myownport;
    }
    public void onClick(View v) {
        Log.v("clicked on send1", "send click");
        //String msg = mTextView.getText().toString() + "\n";

        String msg = localeditViewsend.getText().toString() + "\n";
        Log.v("click on send2", msg);
        localeditViewsend.setText("");
        //mTextView.append("Insert success\n");
        // mTextView.append(msg+"\n");


        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg);
    }

    private class ClientTask extends AsyncTask<String, Void, Void> {


        @Override
        protected  Void doInBackground(String... msgs) {
            try {
                String remotePort = REMOTE_PORT0;

                String msgToSend1 = msgs[0];
                message_obj msg_obj=new message_obj(uniqueno,myPort,msgToSend1);


                ArrayList<String> temparraylist=new ArrayList<String>();
                for(String p:portnos) {
                    temparraylist.add(p);
                }
                GroupMessengerActivity.waitingforproposal.put(uniqueno,temparraylist);
                uniqueno++;
                ArrayList<String> temp=(ArrayList<String>)portnos.clone();
                for(String s:portnos) {
                    temp.remove(s);
                    Log.v("here000","here000");
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(s));
                 //   if (socket.isConnected()) {

                        //socket.setSoTimeout(5000);

                        String msgToSend = msgs[0];

                        Log.v("remotePort", s.toString());
                        Log.v("msgs", msgs.toString());
                        Log.v("socket", socket.toString());
                        Log.v("clienttttttttttt", msgToSend);

                        try{
                        OutputStream out = socket.getOutputStream();


                        ObjectOutputStream oout = new ObjectOutputStream(out);

                        if (oout == null) {
                            Log.v("null sending obj", msg_obj.toString());
                        } else {
                            Log.v("notnull sending obj", msg_obj.toString());
                        }
                        Log.v("sending obj", msg_obj.toString());

                        oout.writeObject(msg_obj);

                        //out.write(msgToSend.getBytes("US-ASCII"));

                        Log.v("out", out.toString());
                /*
                 * TODO: Fill in your client code that sends out a message.
                 */

                        socket.close();
                            Log.v("SOCKETIDinloop", s);

                            Log.v("LOOPclienttaskeach", "Clienttaskeach");
                        } catch (UnknownHostException e) {
                            Log.e("FAILUREclienttask", "ClientTask UnknownHostException");
                            Log.e("FAILed", " " + s);
                            portnos.remove(s);
                            GroupMessengerActivity.handlerfailure(s);

                            for(String g:portnos) {
                                Log.v("print in exception",g);
                            }
                            continue;
                        }
                        catch (IOException e) {
                            Log.e("FAILUREclienttaskio", "ClientTask UnknownHostException");
                            Log.e("FAILedio", " " + s);
                            portnos.remove(s);
                            GroupMessengerActivity.handlerfailure(s);

                            for(String g:portnos) {
                                Log.v("print in exception",g);
                            }


                            for(String k:temp)
                            {
                                Log.v("remaining",k);
                                Socket sockettemp = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(k));
                                OutputStream outtemp = sockettemp.getOutputStream();


                                ObjectOutputStream oouttemp = new ObjectOutputStream(outtemp);
                                oouttemp.writeObject(msg_obj);
                                sockettemp.close();


                            }

                            socket.close();

                            break;
                        }
                        catch(Exception e)
                        {
                            Log.e("FAILUREclienttaskio", "ClientTask UnknownHostException");
                            Log.e("FAILedE", " " + s);
                            GroupMessengerActivity.handlerfailure(s);
                            portnos.remove(s);
                            for(String g:portnos) {
                                Log.v("printt in exception",g);
                            }
                            continue;

                        }

                    //}
                }
            } catch (UnknownHostException e) {
                Log.e("yoyoyoyoclienttask1", "ClientTask UnknownHostException");
                //GroupMessengerActivity.handlerfailure();

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("yoyoyoyoclienttask1", "ClientTask socket IOException");
            }catch(Exception e)
            {
                Log.e("FAILUREclientOUT", "ClientTask UnknownHostException");


                for(String g:portnos) {
                    Log.v("print in exception",g);
                }
               // return null;
            }

            return null;
        }
    }

