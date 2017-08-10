import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SelectTopBiwords {

    private static final int THREAD_COUNT = 8;
    private static final int QUEUE_SIZE = 10000;
    private static final int FINAL_COUNT = 3000000;

    public static void main(String[] args) {
        SelectTopBiwords app = new SelectTopBiwords();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                try {
                    app.execute();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Thread th = new Thread(run);
        th.start();
    }

    public void execute() throws IOException, InterruptedException {

        List<BlockingQueue<String>> queues = new ArrayList<>();
        List<CountProc> procs = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        Random randomGenerator = new Random();

        for (int i = 0; i < THREAD_COUNT; i++) {
            BlockingQueue<String> queue = new ArrayBlockingQueue<String>(QUEUE_SIZE);
            queues.add(queue);
            CountProc proc = new CountProc(queue);
            procs.add(proc);
            Thread thread = new Thread(proc);
            threads.add(thread);
            thread.start();
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;
        long lcount = 0L;
        while((line = br.readLine()) != null) {
            while(!queues.get(randomGenerator.nextInt(THREAD_COUNT)).offer(line)) {
                // nothing to do
            }
            lcount++;
            if (lcount % 100000 == 0) {
                System.err.println("-- now line [" + lcount + "] " + (new Date()));
            }
        }

        for (BlockingQueue<String> q: queues) {
            q.put(CountProc.END_MARK);
        }

        for (Thread t: threads) {
            System.err.println(t.getName() + ": waiting thread to join");
            t.join();
        }

        for (CountProc proc: procs) {
            if (!proc.procEnd) {
                System.err.println("some proc not ended right");
            }
        }


        List<CountProc.CountPair> list = new ArrayList<>();
        for (CountProc proc: procs) {
            list = CountProc.mergeLists(list, proc.sortedList());
        }

        int end = list.size() > FINAL_COUNT ? FINAL_COUNT : list.size();
        for (CountProc.CountPair p : list.subList(0, end)) {
            System.out.println(p.word + " " + p.count);
        }

//        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//        String line;
//        while((line = br.readLine()) != null) {
//            String[] tokens = line.split(" ", 100000);
//            String before = null;
//            for (String tk: tokens) {
//                if (tk != null && !"".equals(tk)) {
//                    boolean excludeOneLetter = false;
//                    if (tk.length() == 1) {
//                        Character c = tk.charAt(0);
//                        excludeOneLetter = excludeOneLetterUnicodeBlock.contains(Character.UnicodeBlock.of(c));
//                    }
//                    if (!excludeOneLetter) {
//                        if (wordcount % 10000000 == 0) {
//                            System.err.println("-- now word [" + wordcount + "] " + (new Date()));
//                        }
//
//                        addToken(tk);
//                        if (before != null) {
//                            addToken(before + "-" + tk);
//                        }
//                        before = tk;
//
//                        wordcount++;
//                    }
//                }
//            }
//        }
//
//        List<CountPair> list = sortedList();
//        int end = list.size() > FINAL_COUNT ? FINAL_COUNT : list.size();
//        for (CountPair p : list.subList(0, end)) {
//            System.out.println(p.word + " " + p.count);
//        }

    }

//    public void execute() {
//        Scanner sc = new Scanner(System.in);
//        while(sc.hasNext()) {
//            String line = sc.nextLine();
//            String[] tokens = line.split(" ");
//            String before = null;
//            for (String tk: tokens) {
//                if (tk != null && !"".equals(tk)) {
//                    boolean excludeOneLetter = false;
//                    if (tk.length() == 1) {
//                        Character c = tk.charAt(0);
//                        excludeOneLetter = excludeOneLetterUnicodeBlock.contains(Character.UnicodeBlock.of(c));
//                    }
//                    if (!excludeOneLetter) {
//                        if (wordcount % 10000000 == 0) {
//                            System.err.println("-- now word [" + wordcount + "] " + (new Date()));
//                        }
//
//                        addToken(tk);
//                        if (before != null) {
//                            addToken(before + "-" + tk);
//                        }
//                        before = tk;
//
//                        wordcount++;
//                    }
//                }
//            }
//        }
//
//        List<CountPair> list = sortedList();
//        int end = list.size() > FINAL_COUNT ? FINAL_COUNT : list.size();
//        for (CountPair p : list.subList(0, end)) {
//            System.out.println(p.word + " " + p.count);
//        }
//
//    }


}
