

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import javazoom.jl.decoder.*;
import javazoom.jl.player.Player;

public class PublisherImpl extends Thread implements Publisher {

    public int port;
    public String ip;
    private int id;
    private ServerSocket providerSocket = null;
    public Socket socket;
    ObjectOutputStream out;
    ObjectInputStream in;
    private static ArrayList<PublisherImpl> publishers = new ArrayList<PublisherImpl>();
    private ArrayList<MusicFile> topics = new ArrayList<>();
    private ArrayList<MusicFile> chunks = new ArrayList<>();
    private static String pathToDataset = "C:/Users/arhsxro/IdeaProjects/dataset1/";


    public PublisherImpl(String ipnew, int portnew, int idnew) {
        ip = ipnew;
        port = portnew;
        id = idnew;
    }

    public ArrayList<MusicFile> getTopics() {
        return topics;
    }

    public void setTopics(ArrayList<MusicFile> topics) {
        this.topics = topics;
    }

    public static void main(String[] args) {
        int numOfPubs = Integer.parseInt(args[0]);
        if(numOfPubs == 0){
            System.out.println("You have chosen to run no Publishers. ");
        }
        else{
            for(int i = 1; i <= numOfPubs; i++) {
                publishers.add(new PublisherImpl("192.168.1.101", 4321+i-1, i));
            }
            for(PublisherImpl p : publishers){
                p.start();
            }
        }
    }

    public void run(){
        Thread t1 = new Thread(){
            public void run(){
                try {
                    PublisherImpl.this.init(id);
                } catch (UnsupportedTagException e) {
                    e.printStackTrace();
                } catch (InvalidDataException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        t1.start();
        this.connect();
    }


    public void init(int x) throws UnsupportedTagException, InvalidDataException, IOException {
        //Contains all the Directories (=genres of music)
        ArrayList<String> genres = new ArrayList<String>();
        File[] files_gen = new File(pathToDataset+"/").listFiles();
        for (File file : files_gen) {
            if (file.isDirectory()) {
                genres.add(file.getName());
            }
        }

        //Reads every file in every directory and instantiates a musicFile object then adds it to msicFiles ArrayList
        String name;
        for(int i = 0; i < genres.size(); i++) {
            File[] files = new File(pathToDataset+"/"+genres.get(i)).listFiles();
            for (File file : files) {
                if (file.isFile()) {
                    name = file.getName();
                    if(!name.startsWith("._")) {
                        name = name.substring(0, name.length()-4);

                        //Get metadata
                        //MusicFile ob = new MusicFile(name,  genres.get(i));
                        Mp3File mp3file = new Mp3File(pathToDataset+"/"+genres.get(i)+"/"+name+".mp3");

                        if (mp3file.hasId3v2Tag()) {
                            ID3v2 id3v2Tag = mp3file.getId3v2Tag();

                            String artist = id3v2Tag.getArtist();

                            //Ignore null values in artist's name
                            if((artist!=null) || (artist=="")) {
                                MusicFile ob = new MusicFile(name, genres.get(i));

                                ob.setArtist(artist);
                                ob.setAlbum(id3v2Tag.getAlbum());
                                ob.setYear(id3v2Tag.getYear());
                                ob.setDuration(mp3file.getLengthInSeconds());
                                ob.setComment(id3v2Tag.getComment());

                                //Separete to publishers
                                String first = artist.substring(0, 1);
                                String div = "N";
                                if(first.compareToIgnoreCase(div) < 0) {
                                    x = 1;
                                    publishers.get(x-1).getTopics().add(ob);
                                }else {
                                    x = 2;
                                    publishers.get(x-1).getTopics().add(ob);
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    public void connect() {
        Socket connection = null;
        String message = null;
        try {
            providerSocket = new ServerSocket(port, 10);

            for (int j = 0; j < brokers.size(); j++) {
                try {
                    connection = providerSocket.accept();
                    out = new ObjectOutputStream(connection.getOutputStream());
                    in = new ObjectInputStream(connection.getInputStream());

                    out.writeObject("publisher " + this.id + " successfully connected to Broker");
                    out.flush();

                    message = (String) in.readObject();

                    System.out.println(connection.getInetAddress().getHostAddress() + "> " + message);
                    int id = Integer.parseInt(message.split(" ")[0]);
                    String ipB = message.split(" ")[1];
                    String portB = message.split(" ")[2];
                    Broker b = new BrokerImpl1(id, ipB, Integer.parseInt(portB));
                    b.setRequestSocket(connection);

                    Value<MusicFile> topic = (Value<MusicFile>) in.readObject();
                    ArrayList<MusicFile> top = topic;
                    b.setTopics(top);
                    b.setRequestSocket(connection);
                    brokers.set(id, b);
                    System.out.println("Broker " + b.getPort() + ":");
                    for (MusicFile t : b.getTopics()) {
                        System.out.print(t.getArtist() + " ");
                    }
                    System.out.println("\n");

                } catch (ClassNotFoundException classnot) {
                    System.err.println("Data received in unknown format");
                }
            }

            while (true) {
                try {
                    connection = providerSocket.accept();
                    out = new ObjectOutputStream(connection.getOutputStream());
                    in = new ObjectInputStream(connection.getInputStream());
                    MusicFile newtopic = (MusicFile) in.readObject();
                    push(newtopic.getArtist(), newtopic.getTitle());


                } catch (Exception e) {
                    notifyFailure();
                    e.printStackTrace();
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }


    public void push(String artist, String song) throws IOException {
        MusicFile mf = new MusicFile();
        Boolean temp = false;
        for (int i = 0; i < this.topics.size(); i++) {
            if (artist.equals(this.topics.get(i).getArtist())) {
                if (song.equals(this.topics.get(i).getTitle())) {
                    temp = true;
                    out.writeObject("Yes");
                    mf = this.topics.get(i);

                    File audioFile = new File(pathToDataset + "/" + mf.getGenre() + "/" + mf.getTitle() + ".mp3");
                    FileInputStream fin = null;
                    fin = new FileInputStream(audioFile);
                    byte fileContent[] = new byte[(int)audioFile.length()];
                    fin.read(fileContent);

                    long total = audioFile.length();
                    long numOfChunks = total / 524288; //512kb
                    out.writeObject(numOfChunks);
                    int offset = -524288;
                    for (int j = 0; j < numOfChunks; j++) {

                        topics.get(0).setAudioChunk(fileContent, offset, j);
                        chunks.add(topics.get(0));

                        offset = offset + 524288;
                    }
                    int c = 0;
                    if(temp){

                        for(MusicFile ch : chunks) {
                            // Player player = new Player(ch.getAudioChunk(c));
                            //player.play();

                            try {
                                out.writeObject(ch);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            c++;
                        }
                    }
                }
        }
        }
        if (temp == false) {
            try {
                out.writeObject("No");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyFailure() {
        try {
            out.writeObject("Publisher node not responding!!!");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void disconnect() {
        try {
            in.close();
            out.close();
            providerSocket.close();

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }
}