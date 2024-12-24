import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

class MyEntry
{
    private Integer key;
    private String value;

    public MyEntry(Integer key, String value) {
        this.key = key;
        this.value = value;
    }

    public Integer getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return key + " " + value;
    }
}

class Node
{
    private int key;
    private String value;
    Node prev, next, above, below;

    public Node (int key, String value)
    {
        this.key = key;
        this.value = value;
    }

    public int getKey ()
    {
        return key;
    }

    public String getValue ()
    {
        return value;
    }
}

class SkipListPQ
{
    private Node s;      // nodo sentinella superiore sinistra
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
        this.s = new Node(Integer.MIN_VALUE, "-inf");
        Node inf = new Node(Integer.MAX_VALUE, "+inf");
        s.next = inf;
        inf.prev = s;
        this.height = 0;
        this.size = 0;
        this.totalTraversed = 0;
        this.totalInserts = 0;
    }

    public int size ()
    {
        return size;
    }

    public MyEntry min()
    {
        if (size == 0) return null;
        Node currentNode = s;
        while (currentNode.below != null)
            currentNode = currentNode.below;
        currentNode = currentNode.next;
        return new MyEntry(currentNode.getKey(), currentNode.getValue());
    }

    public int insert(int key, String value)
    {
        Node currentNode = s;
        int traversedNodes = 0;

        while (currentNode.below != null)
        {
            traversedNodes++;
            currentNode = currentNode.below;
            while (currentNode.next.getKey() < key)
            {
                traversedNodes++;
                currentNode = currentNode.next;
            }
        }

        Node newNode = new Node(key, value);
        Node nextNode = currentNode.next;
        currentNode.next = newNode;
        newNode.prev = currentNode;
        newNode.next = nextNode;
        nextNode.prev = newNode;

        int level = generateEll(alpha, key);
        int currentLevel = 0;

        while (currentLevel < level)
        {
            if (currentLevel >= height)
            {
                height++;
                Node newS = new Node(Integer.MIN_VALUE, "-inf");
                Node newInf = new Node(Integer.MAX_VALUE, "+inf");
                newS.next = newInf;
                newInf.prev = newS;
                newS.below = s;
                s.above = newS;
                s = newS;
            }

            while (currentNode.above == null)
            {
                currentNode = currentNode.prev;
                traversedNodes++;
            }
            traversedNodes++;
            currentNode = currentNode.above;

            Node newAboveNode = new Node(key, value);
            newAboveNode.below = newNode;
            newNode.above = newAboveNode;

            Node aboveNext = currentNode.next;
            currentNode.next = newAboveNode;
            newAboveNode.prev = currentNode;
            newAboveNode.next = aboveNext;
            aboveNext.prev = newAboveNode;

            newNode = newAboveNode;
            currentLevel++;
        }

        size++;
        totalInserts++;
        totalTraversed += traversedNodes;
        return traversedNodes;
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
        {
            currentNode = currentNode.below;
        }
        currentNode = currentNode.next;

        MyEntry min_entry = new MyEntry(currentNode.getKey(), currentNode.getValue());
        
        while (currentNode != null)
        {
            Node next = currentNode.next;
            Node prev = currentNode.prev;
            if (prev != null)
                prev.next = next;
            if (next != null)
                next.prev = prev;
            currentNode = currentNode.above;
        }

        while (s.below != null && s.next.getKey() == Integer.MAX_VALUE)
        {
            s = s.below;
            s.above = null;
            height--;
        }

        size--;
        return min_entry;
    }

    public void print ()
    {
        Node currentNode = s;
        while (currentNode.below != null)
        {
            currentNode = currentNode.below;
        }
        currentNode = currentNode.next;

        List<String> output = new ArrayList<>();
        while (currentNode.getKey() != Integer.MAX_VALUE)
        {
            int h = 1;
            Node temp = currentNode;
            while (temp.above != null)
            {
                h++;
                temp = temp.above;
            }
            output.add(currentNode.getKey() + " " + currentNode.getValue() + " " + h);
            currentNode = currentNode.next;            
        }
        System.out.println(String.join(", ", output));
    }

    public String statistics ()
    {
        double averageTraversedNode = (double) totalTraversed / totalInserts;
        String s = alpha + " " + size + " " + totalInserts + " " + averageTraversedNode;
        return s;        
    }
}

public class TestProgram1
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
            System.out.println(skip_list.statistics());
        } catch (IOException e)
        {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}