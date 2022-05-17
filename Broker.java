import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;

public interface Broker extends Node{

    public void setRequestSocket(Socket soc);
    public void setTopics(ArrayList<MusicFile> t);
    public int getPort();
    public ArrayList<MusicFile> getTopics();
    public String getIp();
    public int getID();
    public void calculateKeys();
    public BigInteger getHashipport();
    public void addTopics(MusicFile t);
    public MusicFile getInfo();
    public void pull(MusicFile p);


}
