import java.util.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;


public class DoubleTransposition {

    // ENGLISH DICTIONARY - Used to score decryption results (higher score = more
    // English-like)
    private static final Set<String> ENGLISH_WORDS_SET = new HashSet<>(Arrays.asList(
            "the", "and", "for", "you", "are", "this", "that", "have", "from", "with",
            "your", "will", "can", "please", "help", "come", "see", "look", "hello",
            "world", "secret", "message", "attack", "meet", "bridge", "night", "day",
            "system", "IT", "course", "computer", "network", "security", "password", "username",
            "is", "to", "of", "in", "it", "on", "at", "by", "be", "he", "she", "we",
            "yes", "no", "good", "bad", "new", "old", "big", "small"));

    // Stores each hacking result for sorting by score
    static class HackResult implements Comparable<HackResult> {
        String rowKey;
        String colKey;
        String text;
        int score;
        int rows, cols;

        HackResult(String rowKey, String colKey, String text, int score, int rows, int cols) {
            this.rowKey = rowKey;
            this.colKey = colKey;
            this.text = text;
            this.score = score;
            this.rows = rows;
            this.cols = cols;
        }

        public int compareTo(HackResult other) {
            return Integer.compare(other.score, this.score);
        }
    }

    // ENCRYPTION: Fill grid row by row, then rearrange rows and columns according to keys
    public static String encrypt(String plaintext, int rows, int cols, String rowKey, String colKey) {
        int totalCells = rows * cols;
        String text = plaintext;

        // Pad with underscores if needed
        if (text.length() < totalCells) {
            text = String.format("%-" + totalCells + "s", text).replace(' ', '_');
        } else {
            text = text.substring(0, totalCells);
        }

        // Fill grid row by row
        char[][] grid = new char[rows][cols];
        int idx = 0;
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                grid[r][c] = text.charAt(idx++);

        // Rearrange rows according to key
        char[][] afterRowSwap = new char[rows][cols];
        for (int r = 0; r < rows; r++) {
            int newRow = Integer.parseInt(String.valueOf(rowKey.charAt(r))) - 1;
            afterRowSwap[r] = grid[newRow];
        }

        // Rearrange columns according to key
        char[][] afterColSwap = new char[rows][cols];
        for (int c = 0; c < cols; c++) {
            int newCol = Integer.parseInt(String.valueOf(colKey.charAt(c))) - 1;
            for (int r = 0; r < rows; r++)
                afterColSwap[r][c] = afterRowSwap[r][newCol];
        }

        // Read ciphertext row by row
        StringBuilder result = new StringBuilder();
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                result.append(afterColSwap[r][c]);

        return result.toString();
    }

    // DECRYPTION: Reverse of encryption process
    public static String decrypt(String ciphertext, int rows, int cols, String rowKey, String colKey) {
        try {
            int totalCells = rows * cols;
            String text = ciphertext;

            if (text.length() < totalCells) {
                text = String.format("%-" + totalCells + "s", text).replace(' ', '_');
            } else if (text.length() > totalCells) {
                text = text.substring(0, totalCells);
            }

            // Single row case
            if (rows == 1) {
                char[] chars = text.toCharArray();
                char[] result = new char[cols];
                for (int i = 0; i < cols && i < colKey.length(); i++) {
                    int pos = Integer.parseInt(String.valueOf(colKey.charAt(i))) - 1;
                    if (pos >= 0 && pos < cols) {
                        result[pos] = chars[i];
                    } else {
                        result[i] = chars[i];
                    }
                }
                return new String(result).replace('_', ' ').trim();
            }

            // Single column case
            if (cols == 1) {
                char[] chars = text.toCharArray();
                char[] result = new char[rows];
                for (int i = 0; i < rows && i < rowKey.length(); i++) {
                    int pos = Integer.parseInt(String.valueOf(rowKey.charAt(i))) - 1;
                    if (pos >= 0 && pos < rows) {
                        result[pos] = chars[i];
                    } else {
                        result[i] = chars[i];
                    }
                }
                return new String(result).replace('_', ' ').trim();
            }

            // Fill grid row by row
            char[][] grid = new char[rows][cols];
            int idx = 0;
            for (int r = 0; r < rows; r++)
                for (int c = 0; c < cols; c++)
                    grid[r][c] = text.charAt(idx++);

            // Reverse column swap
            char[][] revCols = new char[rows][cols];
            for (int c = 0; c < cols && c < colKey.length(); c++) {
                int originalCol = Integer.parseInt(String.valueOf(colKey.charAt(c))) - 1;
                if (originalCol >= 0 && originalCol < cols) {
                    for (int r = 0; r < rows; r++)
                        revCols[r][originalCol] = grid[r][c];
                } else {
                    for (int r = 0; r < rows; r++)
                        revCols[r][c] = grid[r][c];
                }
            }

            // Reverse row swap
            char[][] revRows = new char[rows][cols];
            for (int r = 0; r < rows && r < rowKey.length(); r++) {
                int originalRow = Integer.parseInt(String.valueOf(rowKey.charAt(r))) - 1;
                if (originalRow >= 0 && originalRow < rows) {
                    revRows[originalRow] = revCols[r];
                } else {
                    revRows[r] = revCols[r];
                }
            }

            // Read plaintext row by row
            StringBuilder result = new StringBuilder();
            for (int r = 0; r < rows; r++)
                for (int c = 0; c < cols; c++)
                    result.append(revRows[r][c]);

            return result.toString().replace('_', ' ').trim();

        } catch (Exception e) {
            return "";
        }
    }

    // Score text based on English word count (0-100)
    private static int calculateTextScore(String text) {
        if (text == null || text.length() < 3)
            return 0;

        String[] words = text.toLowerCase().split(" ");
        int validWords = 0;
        int totalWords = 0;

        for (String w : words) {
            StringBuilder cleanBuilder = new StringBuilder();
            for (char c : w.toCharArray()) {
                if (c >= 'a' && c <= 'z') {
                    cleanBuilder.append(c);
                }
            }
            String clean = cleanBuilder.toString();

            if (clean.length() >= 2) {
                totalWords++;
                if (ENGLISH_WORDS_SET.contains(clean)) {
                    validWords++;
                }
            }
        }

        if (totalWords == 0)
            return 0;
        return (validWords * 100) / totalWords;
    }

    // Generates all permutations of 1..n sequentially
    static class PermutationGenerator implements Iterator<String> {
        private final int n;
        private final int[] indices;
        private boolean hasNext;
        private String current;

        public PermutationGenerator(int n) {
            this.n = n;
            this.indices = new int[n];
            for (int i = 0; i < n; i++)
                indices[i] = i;
            this.hasNext = n > 0;
            generateCurrent();
        }

        private void generateCurrent() {
            if (!hasNext) {
                current = null;
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++)
                sb.append(indices[i] + 1);
            current = sb.toString();
        }

        public boolean hasNext() {
            return hasNext;
        }

        public String next() {
            String result = current;
            moveToNext();
            return result;
        }

        // Standard algorithm for next lexicographic permutation
        private void moveToNext() {
            if (!hasNext)
                return;

            int i = n - 2;
            while (i >= 0 && indices[i] >= indices[i + 1])
                i--;

            if (i < 0) {
                hasNext = false;
                current = null;
                return;
            }

            int j = n - 1;
            while (indices[j] <= indices[i])
                j--;

            int temp = indices[i];
            indices[i] = indices[j];
            indices[j] = temp;

            int left = i + 1, right = n - 1;
            while (left < right) {
                temp = indices[left];
                indices[left] = indices[right];
                indices[right] = temp;
                left++;
                right--;
            }
            generateCurrent();
        }
    }

    // TASK 2: Hack with known dimensions - brute force all row! × col! permutations
    public static void hackKnownDimensions(String ciphertext, int rows, int cols) {
        long rowPerms = factorial(rows);
        long colPerms = factorial(cols);
        long totalCombos = rowPerms * colPerms;

        System.out.println("\n" + "=".repeat(60));
        System.out.println("TASK #2: Hacking With Known Dimensions");
        System.out.println("=".repeat(60));
        System.out.println("Ciphertext: " + ciphertext);
        System.out.println("Dimensions: " + rows + " x " + cols);
        System.out.println("Number of Row Permutations (" + rows + "!): " + formatNumber(rowPerms));
        System.out.println("Number of Column Permutations (" + cols + "!): " + formatNumber(colPerms));
        System.out.println("Total Key Combinations Tested: " + formatNumber(totalCombos));
        System.out.println("=".repeat(60));
        System.out.println("Searching...\n");

        java.util.List<HackResult> allRawResults = new ArrayList<>();

        PermutationGenerator rowIter = new PermutationGenerator(rows);
        while (rowIter.hasNext()) {
            String rKey = rowIter.next();
            PermutationGenerator colIter = new PermutationGenerator(cols);
            while (colIter.hasNext()) {
                String cKey = colIter.next();
                String result = decrypt(ciphertext, rows, cols, rKey, cKey);
                int score = calculateTextScore(result);
                allRawResults.add(new HackResult(rKey, cKey, result, score, rows, cols));
            }
        }

        Collections.sort(allRawResults);
        displayResultsInWindow(allRawResults, rows, cols, totalCombos, rowPerms, colPerms, "Task #2");
    }

    // TASK 3: Hack with unknown dimensions - finds both grid size and keys
    public static void hackUnknownDimensions(String ciphertext) {
        int length = ciphertext.length();

        System.out.println("\n" + "=".repeat(70));
        System.out.println("TASK #3: Hacking With Unknown Dimensions");
        System.out.println("=".repeat(70));
        System.out.println("Ciphertext: \"" + ciphertext + "\"");
        System.out.println("Length: " + length);
        System.out.println("=".repeat(70));

        // Find all factor pairs where rows × cols = length
        java.util.List<int[]> allDimensions = new ArrayList<>();
        for (int rows = 1; rows <= length; rows++) {
            if (length % rows == 0) {
                int cols = length / rows;
                if (rows <= cols) {
                    allDimensions.add(new int[] { rows, cols });
                }
            }
        }

        // Filter out impractical dimensions
        java.util.List<int[]> validDimensions = new ArrayList<>();

        System.out.println("\nAnalyzing possible dimensions...");

        for (int[] dim : allDimensions) {
            int rows = dim[0];
            int cols = dim[1];
            long totalCombos = factorial(rows) * factorial(cols);

            if (rows == 1 || cols == 1) {
                System.out.println("  [SKIP] " + rows + " x " + cols + " -> too slow");
                continue;
            }

            if (rows > 10 || cols > 10) {
                System.out.println("  [SKIP] " + rows + " x " + cols + " -> dimensions too large");
                continue;
            }

            if (totalCombos > 10_000_000) {
                System.out.println("  [SKIP] " + rows + " x " + cols + " -> " + formatNumber(totalCombos)
                        + " combinations (too many)");
                continue;
            }

            System.out
                    .println("  [KEEP] " + rows + " x " + cols + " -> " + formatNumber(totalCombos) + " combinations");
            validDimensions.add(dim);
        }

        if (validDimensions.isEmpty()) {
            System.out.println("\n[ERROR] No valid dimensions found!");
            return;
        }

        // Sort by total combinations (fastest first)
        validDimensions.sort((a, b) -> {
            long combosA = factorial(a[0]) * factorial(a[1]);
            long combosB = factorial(b[0]) * factorial(b[1]);
            return Long.compare(combosA, combosB);
        });

        System.out.println("\nDimensions to test (fastest to slowest):");
        for (int[] d : validDimensions) {
            long combos = factorial(d[0]) * factorial(d[1]);
            System.out.println("  " + d[0] + " x " + d[1] + " -> " + formatNumber(combos) + " combinations");
        }

        java.util.List<HackResult> allRawResults = new ArrayList<>();
        long grandTotalCombos = 0;

        // Test each valid dimension
        for (int[] dim : validDimensions) {
            int rows = dim[0];
            int cols = dim[1];
            long rowPerms = factorial(rows);
            long colPerms = factorial(cols);
            long totalCombos = rowPerms * colPerms;

            grandTotalCombos += totalCombos;

            PermutationGenerator rowIter = new PermutationGenerator(rows);
            while (rowIter.hasNext()) {
                String rKey = rowIter.next();
                PermutationGenerator colIter = new PermutationGenerator(cols);
                while (colIter.hasNext()) {
                    String cKey = colIter.next();
                    String result = decrypt(ciphertext, rows, cols, rKey, cKey);
                    int score = calculateTextScore(result);
                    allRawResults.add(new HackResult(rKey, cKey, result, score, rows, cols));
                }
            }
        }

        Collections.sort(allRawResults);
        displayResultsInWindow(allRawResults, validDimensions.size(), grandTotalCombos, "Task #3");
    }

    // Display Task 2 results in popup window
    private static void displayResultsInWindow(java.util.List<HackResult> allResults, int rows, int cols,
            long totalCombos, long rowPerms, long colPerms, String taskName) {

        java.util.List<HackResult> goodResults = new ArrayList<>();
        java.util.List<HackResult> badResults = new ArrayList<>();

        for (HackResult r : allResults) {
            if (r.score >= 30) {
                goodResults.add(r);
            } else {
                badResults.add(r);
            }
        }

        StringBuilder output = new StringBuilder();
        output.append("=".repeat(75)).append("\n");
        output.append("                    HACKING RESULTS - ").append(taskName).append("\n");
        output.append("=".repeat(75)).append("\n\n");

        output.append("=== KEY COMBINATIONS STATISTICS ===\n");
        output.append("Dimensions: ").append(rows).append(" rows x ").append(cols).append(" columns\n");
        output.append("Number of Row Permutations (").append(rows).append("!): ").append(formatNumber(rowPerms))
                .append("\n");
        output.append("Number of Column Permutations (").append(cols).append("!): ").append(formatNumber(colPerms))
                .append("\n");
        output.append("Total Key Combinations Tested: ").append(formatNumber(totalCombos)).append("\n");
        output.append("Good results: ").append(goodResults.size()).append("\n");
        output.append("=".repeat(75)).append("\n\n");

        if (!goodResults.isEmpty()) {
            output.append("=== READABLE RESULTS ===\n");
            output.append("-".repeat(75)).append("\n\n");

            int displayCount = Math.min(50, goodResults.size());
            for (int i = 0; i < displayCount; i++) {
                HackResult r = goodResults.get(i);
                output.append("RESULT #").append(i + 1).append("\n");
                output.append("   Row Key: ").append(r.rowKey).append("\n");
                output.append("   Column Key: ").append(r.colKey).append("\n");
                output.append("   Decrypted Text: \"").append(r.text).append("\"\n");
                output.append("-".repeat(50)).append("\n\n");
            }

            if (goodResults.size() > displayCount) {
                output.append("... and ").append(goodResults.size() - displayCount).append(" more good results\n");
            }
            output.append("\n");
        }

        if (goodResults.isEmpty() && !badResults.isEmpty()) {
            output.append("=== WARNING: No readable English results found (Score < 30) ===\n");
            output.append("Showing ALL raw results (including gibberish) for diagnosis:\n");
            output.append("-".repeat(75)).append("\n\n");

            int displayCount = Math.min(100, badResults.size());
            for (int i = 0; i < displayCount; i++) {
                HackResult r = badResults.get(i);
                output.append("RESULT #").append(i + 1).append("\n");
                output.append("   Row Key: ").append(r.rowKey).append("\n");
                output.append("   Column Key: ").append(r.colKey).append("\n");
                output.append("   Text: \"").append(r.text).append("\"\n");
                output.append("-".repeat(50)).append("\n\n");
            }

            if (badResults.size() > displayCount) {
                output.append("... and ").append(badResults.size() - displayCount).append(" more raw results\n");
            }
        } else if (goodResults.isEmpty() && badResults.isEmpty()) {
            output.append("No results were found. Something went wrong.\n");
        }

        output.append("\n").append("=".repeat(75)).append("\n");
        output.append("                    END OF RESULTS\n");
        output.append("=".repeat(75)).append("\n");

        showResultsWindow(output.toString(), "Hacking Results - " + taskName);
    }

    // Display Task 3 results in popup window
    private static void displayResultsInWindow(java.util.List<HackResult> allResults, int totalDims,
            long grandTotalCombos, String taskName) {

        java.util.List<HackResult> goodResults = new ArrayList<>();
        java.util.List<HackResult> badResults = new ArrayList<>();

        for (HackResult r : allResults) {
            if (r.score >= 30) {
                goodResults.add(r);
            } else {
                badResults.add(r);
            }
        }

        StringBuilder output = new StringBuilder();
        output.append("=".repeat(75)).append("\n");
        output.append("                    HACKING RESULTS - ").append(taskName).append("\n");
        output.append("=".repeat(75)).append("\n\n");

        output.append("=== KEY COMBINATIONS STATISTICS ===\n");
        output.append("Number of dimensions tested: ").append(totalDims).append("\n");
        output.append("Total Key Combinations Tested: ").append(formatNumber(grandTotalCombos)).append("\n");
        output.append("Good results: ").append(goodResults.size()).append("\n");
        output.append("=".repeat(75)).append("\n\n");

        if (!goodResults.isEmpty()) {
            output.append("=== READABLE RESULTS (Score >= 30) ===\n");
            output.append("-".repeat(75)).append("\n\n");

            int displayCount = Math.min(50, goodResults.size());
            for (int i = 0; i < displayCount; i++) {
                HackResult r = goodResults.get(i);
                output.append("RESULT #").append(i + 1).append("\n");
                output.append("   Dimensions: ").append(r.rows).append(" x ").append(r.cols).append("\n");
                output.append("   Row Key: ").append(r.rowKey).append("\n");
                output.append("   Column Key: ").append(r.colKey).append("\n");
                output.append("   Decrypted Text: \"").append(r.text).append("\"\n");
                output.append("-".repeat(50)).append("\n\n");
            }

            if (goodResults.size() > displayCount) {
                output.append("... and ").append(goodResults.size() - displayCount).append(" more good results\n");
            }
            output.append("\n");
        }

        if (goodResults.isEmpty() && !badResults.isEmpty()) {
            output.append("=== WARNING: No readable English results found (Score < 30) ===\n");
            output.append("Showing ALL raw results (including gibberish) for diagnosis:\n");
            output.append("-".repeat(75)).append("\n\n");

            int displayCount = Math.min(100, badResults.size());
            for (int i = 0; i < displayCount; i++) {
                HackResult r = badResults.get(i);
                output.append("RESULT #").append(i + 1).append("\n");
                output.append("   Dimensions: ").append(r.rows).append(" x ").append(r.cols).append("\n");
                output.append("   Row Key: ").append(r.rowKey).append("\n");
                output.append("   Column Key: ").append(r.colKey).append("\n");
                output.append("   Text: \"").append(r.text).append("\"\n");
                output.append("-".repeat(50)).append("\n\n");
            }

            if (badResults.size() > displayCount) {
                output.append("... and ").append(badResults.size() - displayCount).append(" more raw results\n");
            }
        } else if (goodResults.isEmpty() && badResults.isEmpty()) {
            output.append("No results were found. Something went wrong.\n");
        }

        output.append("\n").append("=".repeat(75)).append("\n");
        output.append("                    END OF RESULTS\n");
        output.append("=".repeat(75)).append("\n");

        showResultsWindow(output.toString(), "Hacking Results - " + taskName);
    }

    // Display text in scrollable popup window
    private static void showResultsWindow(String content, String title) {
        JFrame resultFrame = new JFrame(title);
        resultFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        resultFrame.setSize(850, 650);
        resultFrame.setLocationRelativeTo(null);

        JTextArea textArea = new JTextArea(content);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        resultFrame.add(scrollPane);
        resultFrame.setVisible(true);
    }

    // Direct decryption when user knows all keys
    public static void decryptWithKey(String ciphertext, int rows, int cols, String rowKey, String colKey) {
        String result = decrypt(ciphertext, rows, cols, rowKey, colKey);
        System.out.println("\n" + "=".repeat(50));
        System.out.println("DECRYPTION RESULT");
        System.out.println("=".repeat(50));
        System.out.println("Ciphertext: " + ciphertext);
        System.out.println("Dimensions: " + rows + " x " + cols);
        System.out.println("Row Key: " + rowKey);
        System.out.println("Column Key: " + colKey);
        System.out.println("Plaintext: \"" + result + "\"");
        System.out.println("=".repeat(50));
    }

    // Calculate factorial (returns -1 if n > 20)
    private static long factorial(int n) {
        if (n > 20)
            return -1;
        long result = 1;
        for (int i = 2; i <= n; i++)
            result *= i;
        return result;
    }

    // Format large numbers (K, M, B)
    private static String formatNumber(long n) {
        if (n == -1)
            return "Very Large";
        if (n < 1000)
            return String.valueOf(n);
        if (n < 1000000)
            return String.format("%.2fK", n / 1000.0);
        if (n < 1000000000)
            return String.format("%.2fM", n / 1000000.0);
        return String.format("%.2fB", n / 1000000000.0);
    }

    // MAIN MENU
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n" + "=".repeat(60));
        System.out.println("       DOUBLE TRANSPOSITION SYSTEM");
        System.out.println("=".repeat(60));

        while (true) {
            System.out.println("\n" + "-".repeat(40));
            System.out.println("MAIN MENU");
            System.out.println("-".repeat(40));
            System.out.println("1. Encryption");
            System.out.println("2. Decryption");
            System.out.println("3. Hacking Task #2 (known dimensions)");
            System.out.println("4. Hacking Task #3 (unknown dimensions)");
            System.out.println("5. Exit");
            System.out.println("-".repeat(40));
            System.out.print("Choose (1-5): ");

            int mode;
            try {
                mode = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
                continue;
            }
            scanner.nextLine();

            if (mode == 1) {
                System.out.print("Enter plaintext: ");
                String plaintext = scanner.nextLine();
                System.out.print("Number of rows: ");
                int rows = scanner.nextInt();
                System.out.print("Number of columns: ");
                int cols = scanner.nextInt();
                scanner.nextLine();
                System.out.print("Row order: ");
                String rowKey = scanner.nextLine();
                System.out.print("Column order: ");
                String colKey = scanner.nextLine();

                String encrypted = encrypt(plaintext, rows, cols, rowKey, colKey);
                System.out.println("\nEncrypted: \"" + encrypted + "\"");

            } else if (mode == 2) {
                System.out.print("Enter ciphertext: ");
                String ciphertext = scanner.nextLine();
                System.out.print("Number of rows: ");
                int rows = scanner.nextInt();
                System.out.print("Number of columns: ");
                int cols = scanner.nextInt();
                scanner.nextLine();
                System.out.print("Row key: ");
                String rowKey = scanner.nextLine();
                System.out.print("Column key: ");
                String colKey = scanner.nextLine();

                decryptWithKey(ciphertext, rows, cols, rowKey, colKey);

            } else if (mode == 3) {
                System.out.print("Enter ciphertext to hack: ");
                String ciphertext = scanner.nextLine();

                if (ciphertext == null || ciphertext.trim().isEmpty()) {
                    System.out.println("Error: Ciphertext cannot be empty!");
                    continue;
                }

                System.out.print("Enter number of rows: ");
                int rows = scanner.nextInt();
                System.out.print("Enter number of columns: ");
                int cols = scanner.nextInt();
                scanner.nextLine();

                System.out.println("\nUsing dimensions: " + rows + " x " + cols);
                System.out.println("Starting brute force attack...");
                hackKnownDimensions(ciphertext, rows, cols);

            } else if (mode == 4) {
                System.out.print("Enter ciphertext to hack: ");
                String ciphertext = scanner.nextLine();

                if (ciphertext == null || ciphertext.trim().isEmpty()) {
                    System.out.println("Error: Ciphertext cannot be empty!");
                    continue;
                }

                System.out.println("\nStarting brute force attack...");
                hackUnknownDimensions(ciphertext);

            } else if (mode == 5) {
                System.out.println("\nThank you!");
                break;

            } else {
                System.out.println("Invalid choice. Please enter 1-5");
            }
        }
        scanner.close();
    }
}