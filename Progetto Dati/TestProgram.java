import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

class MyEntry
{
    private Integer key;
    private String value;

    public MyEntry (Integer key, String value) {
        this.key = key;
        this.value = value;
    }

    public Integer getKey () {
        return key;
    }

    public String getValue () {
        return value;
    }

    @Override
    public String toString () {
        return key + " " + value;
    }
}

// Classe che rappresenta ogni nodo della SkipList, permette di "linkare" tra di loro i nodi.
// Ogni nodo infatti è caratterizzato dalla entry che contiene ma anche dai collegamenti
// a nodi adiacenti (precedente, successivo, superiore, inferiore)
class Node
{
    private MyEntry entry;     // entry contenuta nel nodo
    Node prev, next, above, below;  // nodi collegati ad un nodo

    public Node (MyEntry entry)
    {
        this.entry = entry;
        initializeNearNodes();
    }

    // metodo getter per ottenere l'entry contenuta nel nodo
    public MyEntry getEntry ()
    {
        return entry;
    }

    public String toString ()
    {
        return entry.toString();
    }

    private void initializeNearNodes ()
    {
        prev = next = above = below = null;
    }
}

class SkipListPQ
{
    private Node s;         // nodo sentinella superiore sinistra
    private int height;     // altezza attuale della skip list
    private int size;       // numero di entry della skip list
    private double alpha;
    private Random rand;
    private int totalTraversed;    // somma totale dei nodi attraversati
    private int totalInserts;      // numero totale di insert

    public SkipListPQ (double alpha)
    {
        this.alpha = alpha;
        this.rand = new Random();
        this.s = new Node(new MyEntry(Integer.MIN_VALUE, "-inf"));
        Node inf = new Node(new MyEntry(Integer.MAX_VALUE, "+inf"));
        // collegamento tra i nodi sentinella
        s.next = inf;
        inf.prev = s;
        // inizializzazione parametri della skipList
        this.height = 0;
        this.size = 0;
        this.totalTraversed = 0;
        this.totalInserts = 0;
    }

    public int size ()
    {
        return size;    // ritorna il numero di entry nella skip list
    }

    public MyEntry min()
    {
        if (size == 0) return null;

        // A partire dal nodo sentinella s scendo fino al livello più basso.
        // La entry con chiave minore è contenuta nel nodo successivo al
        // nodo sentinella di sinistra del livello più basso
        Node currentNode = s;
        while (currentNode.below != null)
            currentNode = currentNode.below;
        return currentNode.next.getEntry();
    }

    public int insert(int key, String value)
    {
        Node currentNode = s;
        int traversedNodes = 0;

        // A partire dal nodo sentinella s mi sposto nei vari livelli fino a quando
        // non incontro una entry con chiave maggiore della chiave della entry da inserire.
        // Quando la trovo mi sposto al livello inferiore e ripeto il procedimento fino al livello più basso.
        while (currentNode.below != null)
        {
            traversedNodes++;
            currentNode = currentNode.below;
            while (currentNode.next.getEntry().getKey() < key)
            {
                traversedNodes++;
                currentNode = currentNode.next;
            }
        }

        // Ora che ho trovato la posizione in cui inserire la nuova entry, creo un nuovo nodo
        // e faccio i doverosi collegamenti tra i due nodi precedentemente adiacenti e il nuovo nodo.
        Node newNode = new Node(new MyEntry(key, value));
        Node nextNode = currentNode.next;
        currentNode.next = newNode;
        newNode.prev = currentNode;
        newNode.next = nextNode;
        nextNode.prev = newNode;

        int level = generateEll(alpha, key);    // numeri di livello in cui inserire la nuova entry
        int currentLevel = 0;

        while (currentLevel < level)
        {
            // Se il livello in cui inserire la nuova entry è maggiore dell'altezza attuale
            // creo un nuovo livello in cui inserisco solo i due nodi sentinella
            if (currentLevel >= height)
            {
                height++;
                Node newS = new Node(new MyEntry(Integer.MIN_VALUE, "-inf"));
                Node newInf = new Node(new MyEntry(Integer.MAX_VALUE, "+inf"));
                newS.next = newInf;
                newInf.prev = newS;
                newS.below = s;
                s.above = newS;
                s = newS;
            }

            // Mi sposto al livello superiore e cerco il nodo precedente al nodo in cui inserire la nuova entry
            while (currentNode.above == null)
            {
                currentNode = currentNode.prev;
                traversedNodes++;
            }
            traversedNodes++;
            currentNode = currentNode.above;

            // Creo il nuovo nodo e faccio i collegamenti tra il nodo nuovo e i nodi superiori ed inferiori...
            Node newAboveNode = new Node(new MyEntry(key, value));
            newAboveNode.below = newNode;
            newNode.above = newAboveNode;
            
            // ... e tra i nodi adiacenti al nodo in cui inserire la nuova entry
            Node aboveNextNode = currentNode.next;
            currentNode.next = newAboveNode;
            newAboveNode.prev = currentNode;
            newAboveNode.next = aboveNextNode;
            aboveNextNode.prev = newAboveNode;

            newNode = newAboveNode;
            currentLevel++;
        }

        // Incremento il numero di entry nella skip list, il numero totale di insert,
        // il totale di nodi attraversati per fare tutti gli insert
        // e ritorno il numero di nodi attraversati per l'insert attuale
        size++;
        totalInserts++;
        totalTraversed += traversedNodes;
        return traversedNodes;  // ritorno il numero di nodi attraversati per fare l'insert della entry in tutti i livelli necessari
    }

    private int generateEll (double alpha_, int key)
    {
        int level = 0;
        if (alpha_ >= 0. && alpha_< 1)
        {
            while (rand.nextDouble() < alpha_)
            {
                level += 1;
            }
        }
        else
        {
            while (key != 0 && key % 2 == 0)
            {
                key = key / 2;
                level += 1;
            }
        }
        return level;
    }

    public MyEntry removeMin()
    {
        if (size == 0) return null;

        Node currentNode = s;
        while (currentNode.below != null)
            currentNode = currentNode.below;
        currentNode = currentNode.next;

        // Salvo la entry da eliminare (che poi restituirò)
        MyEntry min_entry = currentNode.getEntry();
        
        // elimino la entry in tutti i livelli in cui è presente
        // collegando tra loro il precedente e il successivo
        // si tratta di una eliminazione "nascosta"
        while (currentNode != null)
        {
            Node nextNode = currentNode.next;
            Node prevNode = currentNode.prev;
            if (prevNode != null)
                prevNode.next = nextNode;
            if (nextNode != null)
                nextNode.prev = prevNode;
            currentNode = currentNode.above;
        }

        // Se dopo aver eliminato la entry da tutti i livelli mi ritrovo con più
        // di un livello vuoto (ovvero in cui sono presenti solo le due sentinelle)
        // elimino il livello superiore e decremento l'altezza della skip list
        // fino a quando non ho solo un livello vuoto
        while (s.below != null && s.next.getEntry().getKey() == Integer.MAX_VALUE)
        {
            s = s.below;
            s.above = null;
            height--;
        }

        // decremento il numero di entry nella skip list e restituisco la entry eliminata
        size--;
        return min_entry;
    }

    public void print ()
    {
        Node currentNode = s;
        // Mi sposto alla prima entry del livello più basso
        while (currentNode.below != null)
            currentNode = currentNode.below;
        currentNode = currentNode.next;

        String output = "";

        // Per tutte le entry del livello più basso...
        while (currentNode.getEntry().getKey() != Integer.MAX_VALUE)
        {
            // Cerco l'altezza del nodo corrente fermandomi quando un nodo
            // non ha più un superiore
            int h = 1;
            Node temp = currentNode;
            while (temp.above != null)
            {
                h++;
                temp = temp.above;
            }
            output += currentNode + " " + h + ", ";
            currentNode = currentNode.next;            
        }
        output = output.substring(0, output.length() - 2);
        System.out.println(output);
    }

    // Metodo per stampare i parametri della SkipList
    public String values ()
    {
        double averageTraversedNode = (double) totalTraversed / totalInserts;
        String s = alpha + " " + size + " " + totalInserts + " " + averageTraversedNode;
        return s;        
    }
}

public class TestProgram
{
    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("Usage: java TestProgram <file_path>");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(args[0])))
        {
            String[] firstLine = br.readLine().split(" ");
            int N = Integer.parseInt(firstLine[0]);
            double alpha = Double.parseDouble(firstLine[1]);
            System.out.println(N + " " + alpha);

            SkipListPQ skip_list = new SkipListPQ(alpha);

            for (int i = 0; i < N; i++)
            {
                String[] line = br.readLine().split(" ");
                int operation = Integer.parseInt(line[0]);

                switch (operation)
                {
                    case 0:
                        MyEntry min_entry = skip_list.min();
                        if (min_entry != null)
                            System.out.println(min_entry);
                        break;
                    case 1:
                        @SuppressWarnings("unused") MyEntry removed_entry = skip_list.removeMin();
                        break;
                    case 2:
                        int key = Integer.parseInt(line[1]);
                        String value = line[2];
                        skip_list.insert(key, value);
                        break;
                    case 3:
                        skip_list.print();
                        break;
                    default:
                        System.out.println("Invalid operation code");
                        return;
                }
            }
            String s = skip_list.values();
            System.out.println(s);
        } catch (IOException e)
        {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}