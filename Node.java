import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface Node {
    ArrayList<Broker> brokers = new ArrayList<Broker>() {
        {
            add(new BrokerImpl1(0, "192.168.1.101", 1000));
            add(new BrokerImpl2(1, "192.168.1.101", 2000));
            add(new BrokerImpl3(2, "192.168.1.101", 3000));
        }
    };

   public void init(int x) throws UnsupportedTagException, InvalidDataException, IOException;

   public void connect();

   public void disconnect();

}