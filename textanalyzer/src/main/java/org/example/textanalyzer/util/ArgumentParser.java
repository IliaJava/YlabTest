package org.example.textanalyzer.util;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Простой парсер аргументов вида:
 * --dir ./texts --min-length 5 --top 10 --stopwords ./stop.txt --output ./result.json
 */
public class ArgumentParser {

    public static class ParsedArgs {
        private final Map<String, String> values;
        private final boolean help;

        public ParsedArgs(Map<String, String> values, boolean help) {
            this.values = values;
            this.help = help;
        }

        public boolean isHelp() {
            return help;
        }

        public boolean isValid() {
            // Проверяем наличие обязательных параметров
            return values.containsKey("dir")
                    && values.containsKey("min-length")
                    && values.containsKey("top");
        }

        public String getDir() {
            return values.get("dir");
        }

        public int getMinLength() {
            return Integer.parseInt(values.get("min-length"));
        }

        public int getTop() {
            return Integer.parseInt(values.get("top"));
        }

        public Optional<String> getOutputPath() {
            return Optional.ofNullable(values.get("output"));
        }

        public Optional<String> getStopwordsPath() {
            return Optional.ofNullable(values.get("stopwords"));
        }
    }

    public static ParsedArgs parse(String[] args) {
        Map<String, String> map = new HashMap<>();
        boolean help = false;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--help".equals(arg)) {
                help = true;
                break;
            }
            if (arg.startsWith("--")) {
                String key = arg.substring(2);
                // Пытаемся взять следующее значение, если оно не начинается с --
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    map.put(key, args[i + 1]);
                    i++;
                } else {
                    // Флаг без значения (в нашем случае таких нет, но можно расширить)
                    map.put(key, null);
                }
            }
        }

        return new ParsedArgs(map, help);
    }

    public static void printHelp() {
        System.out.println("Использование:");
        System.out.println("  java -jar text-analyzer.jar \\");
        System.out.println("    --dir <path> \\");
        System.out.println("    --min-length <number> \\");
        System.out.println("    --top <number> \\");
        System.out.println("    [--stopwords <path>] \\");
        System.out.println("    [--output <path>] \\");
        System.out.println();
        System.out.println("Параметры:");
        System.out.println("  --dir        Путь к папке с .txt файлами (обязательный)");
        System.out.println("  --min-length Минимальная длина слова (обязательный)");
        System.out.println("  --top        Количество наиболее частых слов (обязательный)");
        System.out.println("  --stopwords  Путь к файлу со стоп-словами (опционально)");
        System.out.println("  --output     Путь к JSON-файлу для вывода результата (опционально)");
        System.out.println("  --help       Показать эту справку");
    }
}
