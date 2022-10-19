# wordInterpritator
Данный проект объединяет в себе возможности Apache POI и Freemarker для генерации Word файлов(docx).
В данный момент на стадии MVP(писалось с приоритетом на скорость, а не на качество, в последствии, при переносе в промышленую ветку, обнеслось тестами и отрефакторенно в соответствии с принципами ООП и СОЛИД), для демонстрации можно просто запустить [psvm](src/main/java/com/sbrf/idrisov/interpritator/Main.java) и результат посмотреть в [result.docx](src/main/resources/result.docx)(после запуска)

## Примеры
### Вход:
[Модель](src/main/resources/forExample.xml)
и сам шаблон
![img.png](img.png)
### Выход:
![img_1.png](img_1.png)
