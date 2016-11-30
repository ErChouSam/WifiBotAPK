package com.example.erchousam.wifibotapk;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
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
    public static String SERVER_IP = "192.168.43.26"; // Adresse ip par defaut pour communiquer avec le wifibot
    TextView state ;                                  // Creation d'un Objet TextView
    Button ConnectionDisconnection;                   // Creation d'un Objet Boutton
    // Creation des Objet ImageButton
    ImageButton TopArrow ;
    ImageButton LeftArrow ;
    ImageButton RightArrow ;
    ImageButton BottomArrow ;
    ImageButton StopButton ;
    boolean sockTrueFalse = false;                    // Test si la socket est connecter ou deconnecter
    Thread Thread1 = null;                            // Thread de connection/deconnection
    Thread ThreadMove = null;

    //Boolean pour connaitre l'etat du thread et le commander
    boolean stateRightThread = true;
    boolean stateLeftThread = true;
    boolean stateTopThread = true;
    boolean stateBotThread = true;

    //Trame a envoyer pour ralentir et arreter le wifibot

    final byte[] StopFrameHigh = new byte[]{(byte)0xFF, 0x07, 0x1E, 0x00, 0x1E, 0x00,(byte)0xF1, (byte)0x08, 0x2C};
    final byte[] StopFrameMid = new byte[]{(byte)0xFF, 0x07, 0x14, 0x00, 0x14, 0x00,(byte)0xF1, (byte)0xB0, 0x2F};
    final byte[] StopFrameLow = new byte[]{(byte)0xFF, 0x07, 0x0A, 0x00, 0x0A, 0x00,(byte)0xF1, (byte)0x78, 0x2B};
    final byte[] StopFrameVeryLow = new byte[]{(byte)0xFF, 0x07, 0x00, 0x00, 0x00, 0x00,(byte)0xF1, (byte)0xC0, 0x28};
    final byte[] StopFrame = new byte[]{(byte)0xFF, 0x07, 0x00, 0x00, 0x00, 0x00,(byte)0xF1, (byte)0xC0, 0x28};
    final byte[] BStopFrameHigh = new byte[]{(byte)0xFF, 0x07, 0x1E, 0x00, 0x1E, 0x00,(byte)0xA1, (byte)0x08, 0x10};
    final byte[] BStopFrameMid = new byte[]{(byte)0xFF, 0x07, 0x14, 0x00, 0x14, 0x00,(byte)0xA1, (byte)0xB0, 0x13};
    final byte[] BStopFrameLow = new byte[]{(byte)0xFF, 0x07, 0x0A, 0x00, 0x0A, 0x00,(byte)0xA1, (byte)0x78, 0x17};
    final byte[] BStopFrameVeryLow = new byte[]{(byte)0xFF, 0x07, 0x00, 0x00, 0x00, 0x00,(byte)0xA1, (byte)0x1C, 0x15};
    final byte[] BStopFrame = new byte[]{(byte)0xFF, 0x07, 0x00, 0x00, 0x00, 0x00,(byte)0xF1, (byte)0xC0, 0x28};



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
        StopButton = (ImageButton)findViewById(R.id.StopButton) ;
        ConnectionDisconnection.setOnClickListener(new View.OnClickListener() {       // Ecoute du bouton ConnectDisconnect
            @Override
            public void onClick(View v) {
                ConnectionDisconnection();
            }
        });

        RightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                stateRightThread = !stateRightThread;
                ThreadMove = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final byte[] Frame = new byte[]{(byte) 0xFF, 0x07, 0x1E, 0x00, 0x1E, 0x00, (byte) 0xE1, 0x09, (byte) 0xE0}; // Trame a envoyer au wifibot
                        DataOutputStream out = null;
                        try {
                            out = new DataOutputStream(sock.getOutputStream());// Création d'un tuyau de communication
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        while (!stateRightThread) {
                            try {
                                out.write(Frame);
                                Thread.sleep(1500);
                                out.flush();// Envoie de la Trame Stop au wifibot
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                if (stateRightThread)
                {
                    DataOutputStream out = null;
                    ThreadMove = null;
                    try {
                        out = new DataOutputStream(sock.getOutputStream());// Création d'un tuyau de communication
                        out.write(StopFrame);
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else
                    ThreadMove.start();
            }
        });
        LeftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final byte[] Frame = {(byte)0xff, 0x07, 0x1E, 0x00, 0x1E, 0x00,(byte)0xB1,0x09,(byte)0xDC}; // Trame a envoyer au wifibot
                stateLeftThread = !stateLeftThread;
                ThreadMove = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DataOutputStream out = null;
                        try {
                            out = new DataOutputStream(sock.getOutputStream());// Création d'un tuyau de communication
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        while (!stateLeftThread) {
                            try {
                                out.write(Frame);
                                out.flush();// Envoie de la Trame Stop au wifibot
                                Thread.sleep(1500);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                if (stateLeftThread) {
                    DataOutputStream out = null;
                    ThreadMove = null;
                    try {
                        out = new DataOutputStream(sock.getOutputStream());// Création d'un tuyau de communication
                        out.write(StopFrame);
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else
                    ThreadMove.start();
            }
        });
        TopArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final byte[] Frame = {(byte)0xff, 0x07, 0x50, 0x00, 0x50, 0x00,(byte)0xF1,(byte)0x00,0x35}; // Trame a envoyer au wifibot
                stateTopThread = !stateTopThread;
                //final Handler TopHandler = new Handler();
                ThreadMove = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DataOutputStream out = null;
                        try {
                            out = new DataOutputStream(sock.getOutputStream());// Création d'un tuyau de communication
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        while (!stateTopThread) {
                            try {
                                out.write(Frame);
                                out.flush();// Envoie de la Trame Stop au wifibot
                                Thread.sleep(1500);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                if (stateTopThread)
                {
                    ThreadMove = null;
                    try {
                        StopMotorForward();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else
                    ThreadMove.start();
            }
        });
        BottomArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final byte[] Frame = {(byte)0xff, 0x07, 0x50, 0x00, 0x50, 0x00,(byte)0xA1,(byte)0x00,(byte)0x09}; // Trame a envoyer au wifibot
                stateBotThread = !stateBotThread;
                ThreadMove = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DataOutputStream out = null;
                        try {
                            out = new DataOutputStream(sock.getOutputStream());// Création d'un tuyau de communication
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        while (!stateBotThread) {
                            try {
                                out.write(Frame);
                                Thread.sleep(1500);
                                out.flush();// Envoie de la Trame Stop au wifibot
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                if (stateBotThread)
                {
                    try {
                        StopMotorBack();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ThreadMove = null;
                }
                else
                    ThreadMove.start();
            }
        });
        StopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataOutputStream out = null;
                //Reset tout les boutons
                if (!stateBotThread)
                    stateBotThread = true;
                if (!stateLeftThread)
                    stateLeftThread = true;
                if (!stateRightThread)
                    stateRightThread = true;
                if (!stateTopThread)
                    stateTopThread = true;
                try {
                    out = new DataOutputStream(sock.getOutputStream());// Création d'un tuyau de communication
                    out.write(StopFrame);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
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
        //Connection ou decoonection SocketTCP
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
    private void setText(final TextView text,final String value,final int color) { // change la couleur et le text de la TextView
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
                text.setTextColor(color);
            }
        });
    }
    private void StopMotorForward() throws IOException, InterruptedException {
        DataOutputStream out = new DataOutputStream(sock.getOutputStream()); // Création d'un tuyau de communication
        final Handler StopHandler = new Handler();
        out.write(StopFrameHigh);
        out.flush();// Envoie de la Trame Stop au wifibot
        Thread.sleep(150);
        out.write(StopFrameMid);
        out.flush();// Envoie de la Trame Stop au wifibot
        Thread.sleep(150);
        out.write(StopFrameLow);
        out.flush();// Envoie de la Trame Stop au wifibot
        Thread.sleep(150);
        out.write(StopFrameVeryLow);
        out.flush();// Envoie de la Trame Stop au wifibot
        Thread.sleep(150);
        out.write(StopFrame);
        out.flush();// Envoie de la Trame Stop au wifibot
    }
    private void StopMotorBack() throws IOException, InterruptedException {
        DataOutputStream out = new DataOutputStream(sock.getOutputStream()); // Création d'un tuyau de communication
        final Handler StopHandler = new Handler();
        out.write(BStopFrameHigh);
        out.flush();// Envoie de la Trame Stop au wifibot
        Thread.sleep(150);
        out.write(BStopFrameMid);
        out.flush();// Envoie de la Trame Stop au wifibot
        Thread.sleep(150);
        out.write(BStopFrameLow);
        out.flush();// Envoie de la Trame Stop au wifibot
        Thread.sleep(150);
        out.write(BStopFrameVeryLow);
        out.flush();// Envoie de la Trame Stop au wifibot
        Thread.sleep(150);
        out.write(BStopFrame);
        out.flush();// Envoie de la Trame Stop au wifibot
    }
}

