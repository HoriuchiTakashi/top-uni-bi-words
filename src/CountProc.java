import java.util.*;
import java.util.concurrent.BlockingQueue;

public class CountProc implements Runnable {
//    private BufferedReader br;
    private BlockingQueue<String> queue;
    public boolean procEnd = false;

    public CountProc(BlockingQueue<String> queue) {
        this.queue = queue;
    }

    public static final String END_MARK = "";

    private static final Set<Character.UnicodeBlock> excludeOneLetterUnicodeBlock;
    static {
        excludeOneLetterUnicodeBlock = new HashSet<>();
        excludeOneLetterUnicodeBlock.add(Character.UnicodeBlock.SUPPLEMENTAL_ARROWS_A);
        excludeOneLetterUnicodeBlock.add(Character.UnicodeBlock.SUPPLEMENTAL_ARROWS_B);
        excludeOneLetterUnicodeBlock.add(Character.UnicodeBlock.SUPPLEMENTAL_MATHEMATICAL_OPERATORS);
        excludeOneLetterUnicodeBlock.add(Character.UnicodeBlock.SUPPLEMENTAL_PUNCTUATION);
        excludeOneLetterUnicodeBlock.add(Character.UnicodeBlock.SUPPLEMENTARY_PRIVATE_USE_AREA_A);
        excludeOneLetterUnicodeBlock.add(Character.UnicodeBlock.SUPPLEMENTARY_PRIVATE_USE_AREA_B);
        excludeOneLetterUnicodeBlock.add(Character.UnicodeBlock.SUPERSCRIPTS_AND_SUBSCRIPTS);
        excludeOneLetterUnicodeBlock.add(Character.UnicodeBlock.SPECIALS);
        excludeOneLetterUnicodeBlock.add(Character.UnicodeBlock.SPACING_MODIFIER_LETTERS);
        excludeOneLetterUnicodeBlock.add(Character.UnicodeBlock.SMALL_FORM_VARIANTS);
        excludeOneLetterUnicodeBlock.add(Character.UnicodeBlock.PRIVATE_USE_AREA);
        excludeOneLetterUnicodeBlock.add(Character.UnicodeBlock.NUMBER_FORMS);
        excludeOneLetterUnicodeBlock.add(Character.UnicodeBlock.MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A);
        excludeOneLetterUnicodeBlock.add(Character.UnicodeBlock.MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B);
        excludeOneLetterUnicodeBlock.add(Character.UnicodeBlock.MISCELLANEOUS_SYMBOLS);
        excludeOneLetterUnicodeBlock.add(Character.UnicodeBlock.MISCELLANEOUS_SYMBOLS_AND_ARROWS);
        excludeOneLetterUnicodeBlock.add(Character.UnicodeBlock.MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS);
        excludeOneLetterUnicodeBlock.add(Character.UnicodeBlock.MISCELLANEOUS_TECHNICAL);
        excludeOneLetterUnicodeBlock.add(Character.UnicodeBlock.MATHEMATICAL_ALPHANUMERIC_SYMBOLS);
        excludeOneLetterUnicodeBlock.add(Character.UnicodeBlock.MATHEMATICAL_OPERATORS);
        excludeOneLetterUnicodeBlock.add(Character.UnicodeBlock.KATAKANA);
        excludeOneLetterUnicodeBlock.add(Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS);
        excludeOneLetterUnicodeBlock.add(Character.UnicodeBlock.HIRAGANA);
        excludeOneLetterUnicodeBlock.add(Character.UnicodeBlock.BASIC_LATIN);
        excludeOneLetterUnicodeBlock.add(Character.UnicodeBlock.ARROWS);
        excludeOneLetterUnicodeBlock.add(Character.UnicodeBlock.AEGEAN_NUMBERS);
        excludeOneLetterUnicodeBlock.add(Character.UnicodeBlock.ALPHABETIC_PRESENTATION_FORMS);
    }

    private static final int MAX_HASH_COUNT = 8000000;
    private static final int RETAIN_COUNT =   4000000;
//    private static final int FINAL_COUNT = 1000000;
//    private static final int MAX_HASH_COUNT = 30;
//    private static final int RETAIN_COUNT = 20;
//    private static final int FINAL_COUNT = 5;

    private long threasholdCount = 0L;
    private boolean firstTime = true;

    private long wordcount = 0L;

    private Map<String, CountPair> countMap = new HashMap<>();
    private TreeSet<CountPair> countSet = new TreeSet<>(new Comparator<CountPair>() {
        @Override
        public int compare(CountPair o1, CountPair o2) {
            if (o1.count > o2.count) {
                return -1;
            }
            if (o1.count < o2.count) {
                return 1;
            }

            return o1.word.compareTo(o2.word);
        }
    });


    @Override
    public void run() {
        String line;
        try {
            while (!END_MARK.equals(line = queue.take())) {
                String[] tokens = line.split(" ", 100000);
                String before = null;
                for (String tk : tokens) {
                    if (tk != null && !"".equals(tk)) {
                        boolean excludeOneLetter = false;
                        if (tk.length() == 1) {
                            Character c = tk.charAt(0);
                            excludeOneLetter = excludeOneLetterUnicodeBlock.contains(Character.UnicodeBlock.of(c));
                        }
                        if (!excludeOneLetter) {
                            if (wordcount % 100000000 == 0) {
                                System.err.println(Thread.currentThread().getName() + ": -- now word [" + wordcount + "] " + (new Date()));
                            }

                            addToken(tk);
                            if (before != null) {
                                addToken(before + "-" + tk);
                            }
                            before = tk;

                            wordcount++;
                        }
                    }
                }
            }

            procEnd = true;

//            List<CountPair> list = sortedList();
//            int end = list.size() > FINAL_COUNT ? FINAL_COUNT : list.size();
//            for (CountPair p : list.subList(0, end)) {
//                System.out.println(p.word + " " + p.count);
//            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void addToken(String tk ) {
        if (tk != null && !"".equals(tk)) {
            // TODO ハッシュにトークンがあるか確認
            CountPair pair = countMap.get(tk);
            // TOOD トークンがあったらとってくる
            // TODO トークンがなかったら初期化してHASHにセットする
            if (pair == null) {
                pair = new CountPair(tk, 0L);
                countMap.put(tk, pair);
            }

            pair.count++;


            // TODO hash のエントリーが threshold を超えているか？
            if ((firstTime && countMap.size() > RETAIN_COUNT) || countMap.size() > MAX_HASH_COUNT) {
//                        System.err.println("start sort");
                System.err.println(Thread.currentThread().getName() + ": -- start sort [" + countMap.size() + "] " + (new Date()));
                List<CountPair> list = sortedList();
                // TODO threshold 更新
                if (list.size() >= RETAIN_COUNT) {
                    threasholdCount = list.get(RETAIN_COUNT - 1).count;
                }

                // TODO ソートしたら足切り
                if (list.size() > RETAIN_COUNT) {
                    for (CountPair p : list.subList(RETAIN_COUNT, list.size())) {
                        countMap.remove(p.word);
//                                System.err.println("-- removed [" + p.word + "] -> [" + countMap.size() + "]");
                    }
                }

//                        System.err.println("now ===== [" + countMap.size() + "]");
//                        for (CountPair p: countMap.values()) {
//                            System.err.println("---> [" + p.word + "] / [" + p.count + "]");
//                        }

                firstTime = false;

                System.err.println(Thread.currentThread().getName() + ": -- end sort [" + countMap.size() + "] " + (new Date()));
            }
        }

    }

    public List<CountPair> sortedList() {
        // TODO 超えていたら エントリーをとってくる
        // TODO 前回 threashold 以上のものを探して LSIT作成
        List<CountPair> list = new ArrayList<>();
        List<CountPair> remList = new ArrayList<>();

        for (CountPair p : countMap.values()) {
            if (p.count >= threasholdCount) {
                list.add(p);
            } else {
                remList.add(p);
            }
        }
        for (CountPair p: remList) {
            countMap.remove(p.word);
//            System.err.println("-- removed [" + p.word + "] -> [" + countMap.size() + "]");
        }

        // TODO ソート
        Collections.sort(list, CountPairComparator.instance);

        return list;
    }

    private static class CountPairComparator implements Comparator<CountPair> {

        public static CountPairComparator instance = new CountPairComparator();

        @Override
        public int compare(CountPair o1, CountPair o2) {
            if (o1.count > o2.count) {
                return -1;
            }
            if (o1.count < o2.count) {
                return 1;
            }
            return o1.word.compareTo(o2.word);
        }

    }

    public static List<CountPair> mergeLists(List<CountPair> list1, List<CountPair> list2) {
        Map<String, CountPair> map = new LinkedHashMap<>();

        List<CountPair> list = new ArrayList<>();
        list.addAll(list1);
        list.addAll(list2);

        for (CountPair p: list) {
            CountPair entity = map.get(p.word);
            if (entity == null) {
                entity = new CountPair(p.word, 0L);
            }
            entity.count += p.count;
            map.put(p.word, entity);
        }

        list = new ArrayList<>(map.values());

        Collections.sort(
                list,
                CountPairComparator.instance
        );

        return list;
    }

    public static class CountPair {
        public String word;
        public Long count;

        public CountPair(String s, Long c) {
            word = s;
            count = c;
        }
    }
}
