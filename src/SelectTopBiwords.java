import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class SelectTopBiwords {

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

    private static final int MAX_HASH_COUNT = 3000000;
    private static final int RETAIN_COUNT = 1500000;
    private static final int FINAL_COUNT = 1000000;
//    private static final int MAX_HASH_COUNT = 50;
//    private static final int RETAIN_COUNT = 10;
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

    public void execute() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while((line = br.readLine()) != null) {
            String[] tokens = line.split(" ", 100000);
            String before = null;
            for (String tk: tokens) {
                if (tk != null && !"".equals(tk)) {
                    boolean excludeOneLetter = false;
                    if (tk.length() == 1) {
                        Character c = tk.charAt(0);
                        excludeOneLetter = excludeOneLetterUnicodeBlock.contains(Character.UnicodeBlock.of(c));
                    }
                    if (!excludeOneLetter) {
                        if (wordcount % 10000000 == 0) {
                            System.err.println("-- now word [" + wordcount + "] " + (new Date()));
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

        List<CountPair> list = sortedList();
        int end = list.size() > FINAL_COUNT ? FINAL_COUNT : list.size();
        for (CountPair p : list.subList(0, end)) {
            System.out.println(p.word + " " + p.count);
        }

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
                System.err.println("-- start sort [" + countMap.size() + "] " + (new Date()));
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

                System.err.println("-- end sort [" + countMap.size() + "] " + (new Date()));
            }
        }

    }

    private List<CountPair> sortedList() {
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
        Collections.sort(
                list,
                (o1, o2) -> {
                    if (o1.count > o2.count) {
                        return -1;
                    }
                    if (o1.count < o2.count) {
                        return 1;
                    }
                    return o1.word.compareTo(o2.word);
                });

        return list;
    }

    private static class CountPair {
        public String word;
        public Long count;

        public CountPair(String s, Long c) {
            word = s;
            count = c;
        }
    }
}
