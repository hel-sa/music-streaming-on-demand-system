import javazoom.jl.decoder.JavaLayerException;

import java.io.FileNotFoundException;

public interface Consumer extends Node{

    public Value<MusicFile> ask(String artist,String category, String song);
    public void playData(Value<MusicFile> fr, int x) throws JavaLayerException, FileNotFoundException;
}
