import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

public class BrokerImpl2 extends Thread implements Broker, Serializable {
    private int id;
    private int port;
    private BigInteger hashipport;
    private String ip;
    private Socket requestSocket;
    ServerSocket replySocket;
    Socket connection = null;
    private ObjectOutputStream outP = null;      //outP is used to communicate with a Publisher
    private ObjectOutputStream outC = null;     //outC is used to communicate with a Consumer
    private ObjectInputStream inP = null;
    private ObjectInputStream inC = null;
    private static ArrayList<MusicFile> tracksList = new ArrayList<>();
    private static String pathToDataset = "C:/Users/arhsxro/IdeaProjects/dataset1";

    private static Value info;
    public ArrayList<MusicFile> topics = new ArrayList<MusicFile>();

    private List<PublisherImpl> registeredPublishers = new ArrayList<PublisherImpl>()
    {
        {add(new PublisherImpl("192.168.1.101", 4321, 1));}
        {add(new PublisherImpl("192.168.1.101", 4322, 2));}
    };


    private List<ConsumerImpl> registeredUsers = new ArrayList<ConsumerImpl>()
    {
    };

    public BrokerImpl2(int id, String ip, int port) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        requestSocket = null;
    }

    public static void main(String[] args) throws InvalidDataException, IOException, UnsupportedTagException {
        int id = Integer.parseInt(args[0]);
        BrokerImpl2 b =(BrokerImpl2) brokers.get(id);
        b.init(id);
        b.start();
    }

    public void setRequestSocket(Socket soc) {
        this.requestSocket = soc;
    }

    public Socket getRequestSocket(){
        return this.requestSocket;
    }
    public int getPort() {
        return this.port;
    }
    public void setTopics(ArrayList<MusicFile> t){
        this.topics = t;
    }
    public void addTopics(MusicFile t){
        this.topics.add(t);
    }

    public ArrayList<MusicFile> getTopics(){
        return this.topics;
    }
    public int getID(){return this.id;}
    public String getIp() {
        return this.ip;
    }
    public BigInteger getHashipport(){
        return this.hashipport;
    }



    public void run()
    {
        /* First the Thread establishes a connection with each Publisher in the registeredPublishers list and informs them of the
         * topics that it is responsible for. */
        for(PublisherImpl p : registeredPublishers) {
            this.acceptConnection(p);
            Value<MusicFile> topic = new Value<>();
            for (MusicFile t : this.topics) {
                topic.add(t);
            }
            this.notifyPublisher(topic);
        }

        /* Then, the Thread establishes a connection with each  Consumer . */
        for(int i = 0; i < 1; i++){
            ConsumerImpl c = null;
            this.acceptConnection(c);
        }

        /* The Thread now awaits for a new request from a consumer. That request is a musicfile object that will be used to create a new Topic
         * named topicAsked. */
        while(true) {
            try{
                Socket connection = replySocket.accept();
                outC = new ObjectOutputStream(connection.getOutputStream());
                inC = new ObjectInputStream(connection.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            MusicFile topicAsked = this.getInfo();

            /* Now, if the topic asked is contained in the Thread's list of topics, method pull is called. */
            for (MusicFile t : this.topics) {
                if (topicAsked.getArtist().equals(t.getArtist()) && topicAsked.getTitle().equals(t.getTitle())) {
                    for(PublisherImpl pub : registeredPublishers) {
                        try {
                            requestSocket = new Socket(pub.ip, pub.port);
                            inP = new ObjectInputStream(requestSocket.getInputStream());
                            outP = new ObjectOutputStream(requestSocket.getOutputStream());

                            this.pull(t);
                        }
                        catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public void acceptConnection(PublisherImpl pub) {
        String publisher;
        try {
            requestSocket = new Socket(pub.ip, pub.port);
            outP = new ObjectOutputStream(requestSocket.getOutputStream());
            inP = new ObjectInputStream(requestSocket.getInputStream());
            pub.socket = requestSocket;
            for(PublisherImpl p : registeredPublishers){
                if(p.port == pub.port && p.ip.equals(pub.ip)){
                    registeredPublishers.set(registeredPublishers.indexOf(p), pub);
                }
            }

            try {
                publisher = (String) inP.readObject();
                System.out.println("\nServer > " + publisher + " " + id);

                outP.writeObject(id + " " + ip + " " + port);
                outP.flush();

            } catch (ClassNotFoundException classNot) {
                System.out.println("data received in unknown format");
            }
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }


    public void acceptConnection(ConsumerImpl con)
    {
        connection = null;
        try {
            replySocket = new ServerSocket(getPort(),10);
            while(true) {
                connection = replySocket.accept();
                String ipC = connection.getInetAddress().getHostAddress();
                int portC = connection.getPort();
                con = new ConsumerImpl(ipC, portC);
                con.requestSocket = connection;
                con.registered = true;
                registeredUsers.add(con);

                outC = new ObjectOutputStream(connection.getOutputStream());
                inC = new ObjectInputStream(connection.getInputStream());

                outC.writeObject("Broker with port " +getPort()  + " successfully connected to Client.");
                outC.flush();

                outC.writeObject(brokers.size());
                for(Broker b : Node.brokers) {
                    Value<MusicFile> topics = new Value<>();
                    outC.writeObject(b.getID());
                    for(MusicFile t : b.getTopics()){
                        topics.add(t);
                    }
                    outC.writeObject(topics);
                }
                break;
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void calculateKeys() {
        String portS = Integer.toString(this.port);
        String temp = ip + portS;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(temp.getBytes());
            byte[] digest = md.digest();
            hashipport = new BigInteger(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public void init(int x) throws UnsupportedTagException, InvalidDataException, IOException {

        for (Broker b : brokers) {
            b.calculateKeys();
        }

        //Contains all the Directories (=genres of music)
        ArrayList<String> genres = new ArrayList<String>();
        File[] files_gen = new File(pathToDataset + "/").listFiles();
        for (File file : files_gen) {
            if (file.isDirectory()) {
                genres.add(file.getName());
            }
        }

        //Reads every file in every directory and instantiates a musicFile object then adds it to msicFiles ArrayList
        String name;
        for (int i = 0; i < genres.size(); i++) {
            File[] files = new File(pathToDataset + "/" + genres.get(i)).listFiles();
            for (File file : files) {
                if (file.isFile()) {
                    name = file.getName();
                    if (!name.startsWith("._")) {
                        name = name.substring(0, name.length() - 4);

                        //Get metadata
                        Mp3File mp3file = new Mp3File(pathToDataset + "/" + genres.get(i) + "/" + name + ".mp3");

                        if (mp3file.hasId3v2Tag()) {
                            ID3v2 id3v2Tag = mp3file.getId3v2Tag();

                            String artist = id3v2Tag.getArtist();

                            //Ignore null values in artist's name
                            if ((artist != null) || (artist == "")) {
                                MusicFile ob = new MusicFile(name, genres.get(i));

                                ob.setArtist(artist);
                                ob.setAlbum(id3v2Tag.getAlbum());
                                ob.setYear(id3v2Tag.getYear());
                                ob.setDuration(mp3file.getLengthInSeconds());
                                ob.setComment(id3v2Tag.getComment());

                                tracksList.add(ob);
                            }

                        }
                    }
                }
            }
        }



        ArrayList<String> artist = new ArrayList<String>();
        ArrayList<String> song = new ArrayList<String>();
        ArrayList<String> genre = new ArrayList<String>();

        for (MusicFile mf : tracksList) {
            artist.add(mf.getArtist());
            song.add(mf.getTitle());
            genre.add(mf.getGenre());

        }

        for (int i = 0; i < artist.size(); i++) {
            BigInteger hashartist = BigInteger.valueOf(0);

            MessageDigest md = null;
            if (artist.get(i) != null) {
                try {
                    md = MessageDigest.getInstance("MD5");
                    md.update(artist.get(i).getBytes());
                    byte[] digest = md.digest();
                    hashartist = new BigInteger(digest);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }


                BigInteger maxhashbroker = BigInteger.valueOf(0);
                ArrayList<BigInteger> brokhash = new ArrayList<>();
                for (Broker b : brokers) {
                    brokhash.add(b.getHashipport());
                }

                //here we add the topic to the appropriate broker.
                int p = 0;
                BigInteger negative = BigInteger.valueOf(-1);
                BigInteger negative1 = BigInteger.valueOf(0);


                    if (hashartist.compareTo(negative1) == -1) {
                        hashartist = hashartist.multiply(negative);
                    }
                    for (int j = 0; j < brokhash.size(); j++) {
                        if (brokhash.get(j).compareTo(negative1) == -1) {
                            brokhash.set(j, brokhash.get(j).multiply(negative));
                        }
                    }

                    BigInteger temp;
                    if (hashartist.compareTo(brokhash.get(p)) == 1) {
                        if (hashartist.compareTo(brokhash.get(p + 1)) == 1) {
                            if (hashartist.compareTo(brokhash.get(p + 2)) == 1) {

                                temp = hashartist.mod(brokhash.get(p));
                            } else {
                                temp = hashartist.mod(brokhash.get(p + 2));
                            }
                        } else {
                            temp = hashartist.mod(brokhash.get(p + 1));
                        }
                    } else {
                        temp = hashartist.mod(brokhash.get(p));
                    }


                int d = 0;
                for(Broker B : brokers) {

                    if (temp.compareTo(brokhash.get(p)) == 1) {
                        if (temp.compareTo(brokhash.get(p + 1)) == 1) {
                            if (temp.compareTo(brokhash.get(p + 2)) == 1) {
                                if(d == 0)
                                    B.addTopics(new MusicFile(artist.get(i),genre.get(i), song.get(i)));
                            } else {
                                if(d == 2)
                                    B.addTopics(new MusicFile(artist.get(i),genre.get(i), song.get(i)));
                            }
                        } else {
                            if(d ==  1)
                                B.addTopics(new MusicFile(artist.get(i),genre.get(i), song.get(i)));
                        }
                    } else {
                        if(d == 0)
                            B.addTopics(new MusicFile(artist.get(i),genre.get(i), song.get(i)));
                    }
                    d++;
                }
            }
        }
    }

    /* This method is used to create a Musicfile object that contains the artist the category and the song the consumer requested. */
    @Override
    public MusicFile getInfo() {
        String artist;
        String song;
        String category;
        MusicFile topic = null;
        try {
            artist = (String) inC.readObject();
            System.out.println(artist);
            category= (String) inC.readObject();
            System.out.println(category);
            song = (String) inC.readObject();
            System.out.println(song);
            topic = new MusicFile(artist,category,song);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return topic;
    }

    public void pull(MusicFile topic) {
        this.notifyPublisher(topic);
        MusicFile reply = null;
        try {
            String repl = (String) inP.readObject();
            if(repl.equals("Yes")) {
                long numOfChunks = (long)inP.readObject();
                outC.writeObject(numOfChunks);
                for(int i = 0; i < numOfChunks; i++) {
                    reply = (MusicFile) inP.readObject();
                    outC.writeObject(reply);
                    outC.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void notifyPublisher(Object p){
        try {
            outP.writeObject(p);
            outP.flush();
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }


    public void connect(){};

    public void disconnect() {
        for (PublisherImpl p : registeredPublishers) {
            try {
                requestSocket = new Socket(p.ip, p.port);
                inP = new ObjectInputStream(requestSocket.getInputStream());
                outP = new ObjectOutputStream(requestSocket.getOutputStream());

                inP.close();
                outP.close();
                requestSocket.close();

                inC.close();
                outC.close();
                replySocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

}