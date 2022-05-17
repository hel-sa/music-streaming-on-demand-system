import java.io.*;

public class MusicFile implements Serializable{
    private String Title;
    private String Genre;
    private String Artist;
    private String Album;
    private String Year;
    private int Duration;
    private String Comment;
    private byte[] musicFileExtract;
    public static final int SIZE = 524288;

    public MusicFile() {}


    public MusicFile(String artist) {
        this.Artist = artist;
    }


    public MusicFile(String Title, String Genre) {
        this.Title = Title;
        this.Genre = Genre;
    }
    public MusicFile(String artist, String Genre, String song) {
        this.Title = song;
        this.Genre = Genre;
        this.Artist = artist;
    }


    public MusicFile(String Title, String Artist, String Album, String Year, int Duration, String Comment) {
        this.Title = Title;
        this.Artist = Artist;
        this.Album = Album;
        this.Year = Year;
        this.Duration = Duration;
        this.Comment = Comment;
    }

    public void setTitle(String Title) {
        this.Title = Title;
    }
    public String getTitle() {
        return Title;
    }

    public void setArtist(String Artist) {
        this.Artist = Artist;
    }
    public String getArtist() {
        return Artist;
    }

    public void setAlbum(String Album) {
        this.Album = Album;
    }
    public String getAlbum() {
        return Album;
    }

    public void setYear(String Year) {
        this.Year = Year;
    }
    public String getYear() {
        return Year;
    }

    public void setDuration(long l) {
        this.Duration = (int) l;
    }
    public int getDuration() {
        return Duration;
    }

    public void setComment(String Comment) {
        this.Comment = Comment;
    }
    public String getComment() {
        return Comment;
    }
    public String getGenre() {
        return Genre;
    }
    public void setAudioChunk(byte[] content,int offset,int chunk_number) throws IOException {

        //Creates a file in specified directory
        OutputStream
                os
                = new FileOutputStream("C:/Users/arhsxro/IdeaProjects/Chunks/"+Title+chunk_number+".mp3");
        os.write(content, offset+SIZE, SIZE);
        os.close();

    }
    public FileInputStream getAudioChunk(int chunk_number) throws FileNotFoundException {

        FileInputStream in = new FileInputStream("C:/Users/arhsxro/IdeaProjects/Chunks/"+Title+chunk_number+".mp3");
        return in;
    }
}
