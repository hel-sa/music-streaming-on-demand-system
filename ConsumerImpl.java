import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;

public class ConsumerImpl implements  Consumer {


    public int port;
    public String ip;
    public Socket requestSocket;
    private ObjectOutputStream out = null;
    private ObjectInputStream in = null;
    public boolean registered = false;
    private Value info;
    MusicFile finalreply;
    Value<MusicFile> fr = new Value<>();
    private  static ArrayList<MusicFile> storedchunks = new ArrayList<>();

    public ConsumerImpl(String ip, int port) {
        ip = ip;
        port = port;
        requestSocket = null;
    }

    public static void main(String[] args) throws FileNotFoundException, JavaLayerException {
        ConsumerImpl con = new ConsumerImpl(" 192.168.1.101", 1234);
        con.connect();
        Scanner reader = new Scanner(System.in);
        int x = 0;

        while(x!=3) {
            System.out.println();
            System.out.println("Enter the artist, the category and the song you want to hear : ");
            String artist = reader.nextLine();
            String category = reader.nextLine();
            String song = reader.nextLine();
            System.out.println("In case you want to disconnect enter 3");
            System.out.println("If you want to store the song to hear it later enter 1,otherwise enter 2");
            x = reader.nextInt();
            con.playData(con.ask(artist, category, song), x);
            if (x == 1) {
                break;
            }

            reader.nextLine();
        }
        con.disconnect();

    }

    //Makes the connection with the brokers and the consumer registers in the right broker.
    public void connect() {
        for (Broker b : brokers) {
            String message;
            try {
                requestSocket = new Socket(InetAddress.getByName(b.getIp()), b.getPort());
                out = new ObjectOutputStream(requestSocket.getOutputStream());
                in = new ObjectInputStream(requestSocket.getInputStream());

                try {
                    message = (String) in.readObject(); //message for successful connection
                    int numBrokers = (int) in.readObject(); //number of brokers
                    int id; //broker's id
                    Value<MusicFile> topic = new Value<>(); //includes the topics of each broker
                    for (int i = 0; i < numBrokers; i++) {
                        id = (Integer) in.readObject();
                        topic = (Value<MusicFile>) in.readObject();
                        ArrayList<MusicFile> top = new ArrayList<MusicFile>();
                        top = topic;
                        Broker bro = brokers.get(id);
                        bro.setTopics(top);
                        brokers.set(id, bro);
                    }
                    for (Broker br : brokers) {
                        System.out.println("Broker > " + message);
                        for (MusicFile t : br.getTopics()) {
                            System.out.print(t.getArtist());
                            System.out.print(" ");
                            System.out.print(t.getGenre());
                            System.out.print(" ");
                            System.out.print(t.getTitle());
                            System.out.print(" ");
                        }
                        System.out.println("---------------------");

                    }

                } catch (ClassNotFoundException classNot) {
                    System.out.println("Data received in unknown format.");
                }
            } catch (UnknownHostException unknownHost) {
                System.err.println("You are trying to connect to an unknown host!");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }


    public Value<MusicFile> ask(String artist,String category, String song) {
        for (Broker b : brokers) {
            try {
                for (MusicFile t : b.getTopics()) {
                    if (t.getArtist().equals(artist) && t.getTitle().equals(song)) {
                        requestSocket = new Socket(InetAddress.getByName(b.getIp()), b.getPort());
                        out = new ObjectOutputStream(requestSocket.getOutputStream());
                        in = new ObjectInputStream(requestSocket.getInputStream());
                        out.writeObject(artist);
                        out.writeObject(category);
                        out.writeObject(song);
                        out.flush();
                        long numOfChunks = (long) in.readObject();
                        for(int i = 0; i < numOfChunks; i++) {
                            finalreply = (MusicFile) in.readObject(); //finalreply has all the info and 1 chunk.
                            fr.add(finalreply);
                        }

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return fr;
    }

    public void playData(Value<MusicFile> fr, int x) throws JavaLayerException, FileNotFoundException {
        int c = 0;
        if(x == 2) {
            for (MusicFile m : fr) {
                Player player = new Player(m.getAudioChunk(c));
                player.play();
                c++;
            }
        }else {
            for (MusicFile m : fr) // store the chunks to a lsit so to listen it later
                storedchunks.add(m);
        }
    }


    public void disconnect() {
        for (Broker b : brokers) {
            try {
                in.close();
                out.close();
                requestSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public void init(int x){};
}
