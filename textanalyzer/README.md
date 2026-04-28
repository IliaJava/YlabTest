# Text-Analyzer
Консольное приложение для анализа текстовых файлов.

## Сборка
bash
mvn clean package
В результате будет создан  файл target/text-analyzer-3.5.14.jar.

## Запуск
1.Запустить можно из Idea: Run->Edit Configurations->Program Arguments
(указываем параметры например --dir C:\ --min-length 5 --top 10) будут прочитаны
все txt файлы из корня диска С. 

2.Можно через командную строку
для удобства создана папка exaplework, где лежат текстовые файлы и переименованный
файл text-analyzer.jar
Пример запуска может быть такой, если находимся в папке examlework.
.\examplework>java -jar text-analyzer.jar --dir .\ --min-length 5 --top 10 --stopwords ./stop.txt

## Результат
2026-04-28T23:29:42.244+07:00 DEBUG 11752 --- [textanalyzer] [           main] o.e.t.service.TextAnalysisServiceImpl    : Processing file: .\stop.txt
2026-04-28T23:29:42.247+07:00 DEBUG 11752 --- [textanalyzer] [           main] o.e.t.service.TextAnalysisServiceImpl    : Processing file: .\text1.txt
2026-04-28T23:29:42.274+07:00 DEBUG 11752 --- [textanalyzer] [           main] o.e.t.service.TextAnalysisServiceImpl    : Processing file: .\text2.txt
1. голос - 2
2. черты - 2
3. авроры - 1
4. безнадежной - 1
5. бледное - 1
6. вечор - 1
7. взоры - 1
8. виденье - 1
9. вьюга - 1
10. гений - 1