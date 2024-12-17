import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

//Class my entry
class MyEntry
{
    private Integer key;
    private String value;

    public MyEntry (Integer key, String value)
    {
        this.key = key;
        this.value = value;
    }
    public Integer getKey ()
    {
        return key;
    }
    public String getValue ()
    {
        return value;
    }
    @Override
    public String toString ()
    {
        return key + " " + value;
    }
}

class SkipListPQ
{
    private double alpha;
    private Random rand;
    private List<List<MyEntry>> skip_list;
    private int size;

    public SkipListPQ (double alpha)
    {
        this.alpha = alpha;
        this.rand = new Random();
        this.size = 0;
        this.skip_list = new ArrayList<>();
        
        List<MyEntry> base_level = new ArrayList<>();
        base_level.add(new MyEntry(Integer.MIN_VALUE, "-inf"));
        base_level.add(new MyEntry(Integer.MAX_VALUE, "+inf"));
        skip_list.add(base_level);
    }

    public int size () 
    {
        return size;
    }

    public MyEntry min ()
    {
        if (size == 0) return null;
        return skip_list.get(0).get(1);
    }
    
    public int insert (int key, String value)
    {
        int[] v = SkipSearch(key);
        int p = v[0];
        int traversed_nodes = v[1];

        MyEntry new_entry = new MyEntry(key, value);
        int level = generateEll(alpha, key);

        while (level >= skip_list.size())
        {
            List<MyEntry> new_level = new ArrayList<>();
            new_level.add(new MyEntry(Integer.MIN_VALUE, "-inf"));
            new_level.add(new MyEntry(Integer.MAX_VALUE, "+inf"));
            skip_list.add(new_level);
        }

        for (int i = 0; i <= level; i++)
        {
            List<MyEntry> current_level = skip_list.get(i);
            current_level.add(p + 1, new_entry);
        }

        size++;
        return traversed_nodes;          
    }

    private int[] SkipSearch (int key)
    {
        int p = 0;
        int traversed_nodes = 0;

        for (int i = skip_list.size() - 1; i >= 0; i--)
        {
            List<MyEntry> level = skip_list.get(i);
            while (p < level.size() - 1 && level.get(p + 1).getKey() <= key)
            {
                p++;
                traversed_nodes++;
            }
            traversed_nodes++;
            if (i > 0)
            {
                p = Math.max(0, p - 1);
            }
        }
        return new int[] {p, traversed_nodes};
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
        
        MyEntry min_entry = min();
        for (int i = 0; i < skip_list.size(); i++)
        {
            List<MyEntry> level = skip_list.get(i);
            if (level.contains(min_entry))
            {
                level.remove(min_entry);
            }
            else
            {
                break;
            }
        }

        while (skip_list.size() > 1 && skip_list.get(skip_list.size() - 1).size() == 2)
        {
            skip_list.remove(skip_list.size() - 1);
        }

        size--;
        return min_entry;
    }
    
    public void print()
    {
        List<MyEntry> bottom_level = skip_list.get(0);

        for (int i = 1; i < bottom_level.size() - 1; i++) {
            MyEntry entry = bottom_level.get(i);
            int height = 1;

            for (int j = 1; j < skip_list.size(); j++) {
                if (skip_list.get(j).contains(entry)) {
                    height++;
                }
            }
            System.out.print(entry.getKey() + " " + entry.getValue() + " " + height);
            
            if (i < bottom_level.size() - 2) {
                System.out.print(", ");
            }
        }
        System.out.println();
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
            int total_inserts = 0;
            int total_traversed = 0;

            for (int i = 0; i < N; i++)
            {
                String[] line = br.readLine().split(" ");
                int operation = Integer.parseInt(line[0]);

                switch (operation)
                {
                    case 0:
                        MyEntry min_entry = skip_list.min();
                        if (min_entry != null) {
                            System.out.println(min_entry);
                        }
                        break;
                    case 1:
                        @SuppressWarnings("unused") MyEntry removedEntry = skip_list.removeMin();
                        break;
                    case 2:
                        int key = Integer.parseInt(line[1]);
                        String value = line[2];
                        int node_traversed = skip_list.insert(key, value);
                        total_traversed += node_traversed;
                        total_inserts++;
                        break;
                    case 3:
                        skip_list.print();
                        break;
                    default:
                        System.out.println("Invalid operation code");
                        return;
                }
            }

            double average_traversed_node = (double) total_traversed / total_inserts;
            System.out.println(alpha + " " + skip_list.size() + " " + total_inserts + " " + average_traversed_node);
        } catch (IOException e)
        {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}