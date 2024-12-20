import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

class MyEntry
{
    private Integer key;
    private String value;
    private boolean isFake;

    public MyEntry (Integer key, String value)
    {
        this.key = key;
        this.value = value;
        this.isFake = false;
    }
    public MyEntry (Integer key, String value, boolean isFake)
    {
        this.key = key;
        this.value = value;
        this.isFake = isFake;
    }
    public Integer getKey ()
    {
        return key;
    }
    public String getValue ()
    {
        return value;
    }
    public boolean isFake ()
    {
        return isFake;
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
        
        List<MyEntry> default_level = new ArrayList<>();
        default_level.add(new MyEntry(Integer.MIN_VALUE, "-inf"));
        default_level.add(new MyEntry(Integer.MAX_VALUE, "+inf"));
        skip_list.add(default_level);
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

    public int insert(int key, String value) {
        int[] v = SkipSearch(key);
        int p = v[0];
        int traversed_nodes = v[1];
    
        MyEntry new_entry = new MyEntry(key, value);
        int height = generateEll(alpha, key);
    
        while (height >= skip_list.size()) {
            List<MyEntry> new_level = new ArrayList<>();
            new_level.add(new MyEntry(Integer.MIN_VALUE, "-inf"));
            new_level.add(new MyEntry(Integer.MAX_VALUE, "+inf"));
            skip_list.add(new_level);
        }

        for (int i = 0; i <= height; i++) {
            List<MyEntry> level = skip_list.get(i);
        
            while (level.size() <= p + 1) {
                level.add(level.size() - 1, new MyEntry(-1, "null", true));
            }
        
            level.add(p + 1, new_entry);
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

            while (p < level.size() - 1 && level.get(p + 1).getKey() <= key) {
                p++;
                if (!level.get(p).isFake())
                    traversed_nodes++;
            }

            if (!level.get(p).isFake()) {
                traversed_nodes++;
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

        int i = 0;
        while (i < skip_list.size())
        {
            if (skip_list.get(i).contains(min_entry))
                skip_list.get(i).remove(min_entry);
            else
                break;
            i++;
        }

        while (skip_list.size() > 1 && skip_list.get(skip_list.size() - 1).size() == 2)
            skip_list.remove(skip_list.size() - 1);

        size--;
        return min_entry;
    }
    
    public void print()
    {
        String s = "";
        for (int i = 1; i < skip_list.get(0).size() - 1; i++) {
            MyEntry entry = skip_list.get(0).get(i);
            int height = 1;

            for (int j = 1; j < skip_list.size(); j++)
                if (skip_list.get(j).contains(entry))
                    height++;
            
            s += entry + " " + height + ", ";
        }
        System.out.println(s);
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
                        if (min_entry != null)
                            System.out.println(min_entry);
                        break;
                    case 1:
                        @SuppressWarnings("unused") MyEntry removed_entry = skip_list.removeMin();
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