package com.example.erchousam.wifibotapk;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    Socket sock;                                      // Socket Client
    private View viewf;                               // View de la seconde activité
    public static int SERVERPORT = 15020;             // Port par defaut pour communiquer avec le wifibot
    public static String SERVER_IP = "192.168.1.106"; // Adresse ip par defaut pour communiquer avec le wifibot
    TextView state ;                                  // Creation d'un Objet TextView
    Button ConnectionDisconnection;                   // Creation d'un Objet Boutton
    ImageButton TopArrow ;                            // Creation d'un Objet ImageButton
    ImageButton LeftArrow ;                           // Creation d'un Objet ImageButton
    ImageButton RightArrow ;                          // Creation d'un Objet ImageButton
    ImageButton BottomArrow ;                         // Creation d'un Objet ImageButton
    boolean sockTrueFalse = false;                    // Test si la socket est connecter ou deconnecter
    Thread Thread1 = null;                            // Thread de connection/deconnection
    final byte[] StopFrame = new byte[]{(byte)0xFF, 0x07, 0x00, 0x00, 0x00, 0x00,(byte)0xF1, (byte)0xC0, 0x28};// Trame a envoyer pour arreter le robot



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        state = (TextView)findViewById(R.id.Status);                                  // Instance du TextView qui indique si connecter ou pas
        ConnectionDisconnection = (Button) findViewById(R.id.ConnectionDisconnection);// Instance du Boutton de connection / deconnection
        TopArrow = (ImageButton)findViewById(R.id.TopButton);                         // Instance du BouttonImage d'une fleche vers le haut
        LeftArrow = (ImageButton)findViewById(R.id.LeftButton);                       // Instance du BouttonImage d'une fleche vers la gauche
        RightArrow = (ImageButton)findViewById(R.id.RightButton);                     // Instance du BouttonImage d'une fleche vers la droite
        BottomArrow = (ImageButton)findViewById(R.id.BotButton);                      // Instance du BouttonImage d'une fleche vers le bas
        ConnectionDisconnection.setOnClickListener(new View.OnClickListener() {       // Ecoute du bouton ConnectDisconnect
            @Override
            public void onClick(View v) {
                ConnectionDisconnection();
            }
        });
        /*TopArrow.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                Thread thread = new Thread(new Runnable(){

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        while((v.isPressed()))
                        {
                            MovingForward(v);
                        }
                    }
                });
                thread.start();

                return false;
            }
        });
        BottomArrow.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                Thread thread = new Thread(new Runnable(){

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        while((v.isPressed()))
                        {
                            MoveBack(v);
                        }
                    }
                });
                thread.start();
                return false;
            }
        });
        LeftArrow.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                Thread thread = new Thread(new Runnable(){

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        while((v.isPressed()))
                        {
                            TurnLeft(v);
                        }
                    }
                });
                thread.start();
                return false;
            }
        });
        /*RightArrow.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                Thread thread = new Thread(new Runnable(){

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        while((v.isPressed()))
                        {
                            TurnRight(v);
                        }
                    }
                });
                thread.start();
                return false;
            }
        });*/
        RightArrow.setOnTouchListener(new View.OnTouchListener() { // Ecoute du BoutonImage Fleche droite
            @Override
            public boolean onTouch(View v, MotionEvent me) {
                final byte[] Frame = new byte[]{(byte)0xFF, 0x07, 0x1E, 0x00, 0x1E, 0x00,(byte) 0xE1, 0x09, (byte)0xE0}; // Trame a envoyer au wifibot
                switch (me.getAction()) {
                    case MotionEvent.ACTION_DOWN: // Tant que l'on reste appuyé
                        try {
                            DataOutputStream out = new DataOutputStream(sock.getOutputStream()); // Création d'un tuyau de communication
                            out.write(Frame);                                                    // Envoie de la Trame au wifibot
                            out.flush();                                                         // Force tout les octects a être écrit
                            out.close();                                                         // Ferme le tuyau de communication
                        } catch (IOException e) {                                                // Exeption en cas d'erreur
                            e.printStackTrace();
                        }
                        break;
                    case MotionEvent.ACTION_UP: // Quand on relache
                        try {
                            DataOutputStream out = new DataOutputStream(sock.getOutputStream()); // Création d'un tuyau de communication
                            out.write(StopFrame);                                                // Envoie de la Trame Stop au wifibot
                            out.flush();
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                }
                return false;
            }
        });
        LeftArrow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent me) {
                final byte[] Frame = {(byte)0xff, 0x07, 0x1E, 0x00, 0x1E, 0x00,(byte)0xB1,0x09,(byte)0xDC};
                switch (me.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        try {
                            DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                            out.write(Frame);
                            out.flush();
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        try {
                            DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                            out.write(Frame);
                            out.flush();
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //appuyé et bouge
                        break;
                    case MotionEvent.ACTION_UP:
                        try {
                            DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                            out.write(StopFrame);
                            out.flush();
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //relaché
                        break;
                }
                return false;
            }
        });
        TopArrow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent me) {
                final byte[] Frame = {(byte)0xff, 0x07, 0x50, 0x00, 0x50, 0x00,(byte)0xF1,(byte)0x00,0x35};
                switch (me.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //appuyé
                        try {
                            DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                            out.write(Frame);
                            out.flush();
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        try {
                            DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                            out.write(Frame);
                            out.flush();
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //appuyé et bouge
                        break;
                    case MotionEvent.ACTION_UP:
                        try {
                            DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                            out.write(StopFrame);           // write the messag
                            out.flush();
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //relaché
                        break;
                }
                return false;
            }
        });
        BottomArrow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent me) {
                final byte[] Frame = {(byte)0xff, 0x07, 0x50, 0x00, 0x50, 0x00,(byte)0xA1,(byte)0x00,0x09};
                switch (me.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //appuyé
                        try {
                            DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                            out.write(Frame);           // write the messag
                            out.flush();
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        try {
                            DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                            out.write(Frame);           // write the messag
                            out.flush();
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //appuyé et bouge
                        break;
                    case MotionEvent.ACTION_UP:
                        try {
                            DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                            out.write(StopFrame);           // write the messag
                            out.flush();
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //relaché
                        break;
                }
                return false;
            }
        });
       /* RightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TurnRight(v);
            }
        });*/

    }
    public static void setServerPort(int port) // Accesseur pour le port
    {
        SERVERPORT = port;
    }

    public static void setServerIp(String IpAdress) // Accesseur pour l'adresse IP
    {
        SERVER_IP = IpAdress;
    }

    public void PageSwitch(View view) // Change d'activité
    {
        Intent SwitchPage = new Intent(MainActivity.this,SecondActivity.class);
        startActivity(SwitchPage);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // Menu Connection
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_Parametre) {
            PageSwitch(viewf);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void ConnectionDisconnection()
    {
        Thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                setText(state,"Connexion . . . . ",Color.YELLOW);
                if(!sockTrueFalse) {
                    try {
                        InetAddress.getByName(SERVER_IP).isReachable(5);
                        sock = new Socket(SERVER_IP,SERVERPORT);
                        if(sock.isConnected()){
                            sockTrueFalse = true;
                            setText(state,"Connected",Color.GREEN);
                        }

                    } catch (UnknownHostException e1) {
                        e1.printStackTrace();
                        setText(state,"Erreur 1" + e1.getMessage(),Color.BLACK);
                    } catch (IOException e1) {
                        setText(state,"Erreur 2" + e1.getMessage(),Color.BLACK);
                        e1.getMessage();
                        e1.printStackTrace();
                    }
                }
                else if(sockTrueFalse)
                {
                    try {
                        sock.close();
                        setText(state,"Disconnected",Color.RED);
                        sockTrueFalse = false;
                    } catch (UnknownHostException e1) {
                        e1.printStackTrace();
                        setText(state,"Erreur 1" + e1.getMessage(),Color.BLACK);
                    } catch (IOException e1) {
                        setText(state,"Erreur 2" + e1.getMessage(),Color.BLACK);
                        e1.getMessage();
                        e1.printStackTrace();
                    }
                }
            }
        });
        Thread1.start();
    }
    /*private void TurnRight(final View v)
    {
        final byte[] Frame = new byte[]{(byte)0xFF, 0x07, 0x1E, 0x00, 0x1E, 0x00,(byte) 0xE1, 0x09, (byte)0xE0};
        /*Thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    while ((v.isPressed())) {
                        try {
                            DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                            out.write(Frame);           // write the messag
                            out.close();
                        } catch (UnknownHostException e) {

                            e.printStackTrace();

                        } catch (IOException e) {

                            e.printStackTrace();

                        } catch (Exception e) {

                            e.printStackTrace();

                        }
                    }
                }
                catch (Exception e)
                {

                }
            }
        });
        Thread2.start();*/

    /*}
    private void TurnLeft(final View v)
    {
        final byte[] Frame = {(byte)0xff, 0x07, 0x1E, 0x00, 0x1E, 0x00,(byte)0xB1,0x09,(byte)0xDC};
        Thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    while ((v.isPressed())) {
                        try {
                            DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                            out.write(Frame);           // write the message
                            out.flush();
                            out.close();
                        } catch (UnknownHostException e) {

                            e.printStackTrace();

                        } catch (IOException e) {

                            e.printStackTrace();

                        } catch (Exception e) {

                            e.printStackTrace();

                        }
                    }
                }
                catch (Exception e)
                {

                }
            }
        });
        Thread2.start();
    }
    private void MovingForward(final View v)
    {
        final byte[] Frame = {(byte)0xff, 0x07, 0x50, 0x00, 0x50, 0x00,(byte)0xF1,(byte)0x00,0x35};
        Thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    while ((v.isPressed())) {
                        try {
                            DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                            out.write(Frame);           // write the message
                            out.flush();
                            out.close();
                        } catch (UnknownHostException e) {

                            e.printStackTrace();

                        } catch (IOException e) {

                            e.printStackTrace();

                        } catch (Exception e) {

                            e.printStackTrace();

                        }
                    }
                }
                catch (Exception e)
                {

                }
            }
        });
        Thread2.start();
    }
    private void MoveBack(final View v)
    {
        final byte[] Frame = {(byte)0xff, 0x07, 0x50, 0x00, 0x50, 0x00,(byte)0xA1,(byte)0x00,0x09};
        Thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    while ((v.isPressed())) {
                        try {
                            DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                            out.write(Frame);           // write the message
                            out.flush();
                            out.close();
                        } catch (UnknownHostException e) {

                            e.printStackTrace();

                        } catch (IOException e) {

                            e.printStackTrace();

                        } catch (Exception e) {

                            e.printStackTrace();

                        }
                    }
                }
                catch (Exception e)
                {

                }
            }
        });
        Thread2.start();
    }*/
    private void setText(final TextView text,final String value,final int color){ // change la couleur et le text de la TextView
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
                text.setTextColor(color);
            }
        });
    }
}

