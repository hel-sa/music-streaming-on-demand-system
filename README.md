# music-streaming-on-demand-system
A multi-threaded, distributed publish-subscribe system that develops a music streaming on demand system with the use of Java 8

Dependencies:
Για να τρέξετε την εργασία θα χρειαστείτε τα ακόλουθα dependencies στο αντίστοιχο pom.xml 
αρχείο που παράγεται με την δημιουργία ενός Maven Project για την διαχείρηση των mp3 
αρχείων:
<dependencies>
 <dependency>
 <groupId>com.mpatric</groupId>
 <artifactId>mp3agic</artifactId>
 <version>0.9.1</version>
</dependency>
 <dependency>
 <groupId>junit</groupId>
 <artifactId>junit</artifactId>
 <version>3.8.1</version>
 <scope>test</scope>
 </dependency>
 <dependency>
 <groupId>javazoom</groupId>
 <artifactId>jlayer</artifactId>
 <version>1.0.1</version>
 </dependency>
</dependencies><dependencies>
 <dependency>
 <groupId>com.mpatric</groupId>
 <artifactId>mp3agic</artifactId>
 <version>0.9.1</version>
</dependency>
 <dependency>
 <groupId>junit</groupId>
 <artifactId>junit</artifactId>
 <version>3.8.1</version>
 <scope>test</scope>
 </dependency>
 <dependency>
 <groupId>javazoom</groupId>
 <artifactId>jlayer</artifactId>
 <version>1.0.1</version>
 </dependency>
</dependencies>
 Dataset:
http://83.212.207.18:9095/dataset1.zip
Από το dataset αφαιρέσαμε 3 τραγούδια: 
• Awkward Apocalyptic Pickup.mp3 -> Miscellaneaous
• Burning Trapezoid of Fire.mp3 -> Miscellaneaous
• Heroic Adventure.mp3 -> Epic Dramatic
Τα συγκεκριμένα τραγούδια είχαν πρόβλημα αναπαραγωγής και δημιουργούσαν θέμα στη 
διαχείρηση τους καθώς περιείχαν τιμές, οι οποίες δεν ήταν ούτε null ούτε Unknown
 Run:
Για να τρέξετε την εργασία η σειρά εκτέλεσης των κλάσεων είναι: 
PublisherImpl.java > BrokerImpl1.java > BrokerImpl2.java > BrokerImpl3.java > Consumer.java
*Με το τρέξιμο της κλάσης Consumer θα σας ζητηθεί να δώσετε το όνομα του καλλιτέχνη, 
την κατηγορία στην οποία ανήκει το τραγούδι καθώς και το όνομα του τραγουδιού που 
θέλετε να ακούσετε (με τη σειρά αυτή)
