import java.io.IOException;
import java.util.ArrayList;

public interface Publisher extends Node {

    public ArrayList<MusicFile> getTopics();
    public void setTopics(ArrayList<MusicFile> topics);
    public void push(String artist, String song) throws IOException;
    public void notifyFailure();


    }
