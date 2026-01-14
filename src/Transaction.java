import java.io.Serializable;      //Работа с вводом-выводом сериализированных данных
import java.time.LocalDateTime;   //Привязка к текущей дате и времени

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String type; // "INCOME" или "EXPENSE"
    private String category;
    private double amount;
    private String description;
    private LocalDateTime date;
    private String userId;
    //Конструктор транзакций
    public Transaction(String type, String category, double amount, String description, String userId) {
        this.id = java.util.UUID.randomUUID().toString();  //Автогенерация ID для пользователей
        this.type = type;                                  //Тип транзакции доход/расход
        this.category = category;                          //категория зарплата/еда и т.п.
        this.amount = amount;                              //сумма
        this.description = description;                    //описание, например аванс или зарплата "за январь"
        this.userId = userId;                              //имя пользователя
        this.date = LocalDateTime.now();                   //текущее время
    }

    // Геттеры и сеттеры
    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getUserId() {
        return userId;
    }
    //закрепляем тему наследования обжект. Переопределяем метод toString() и формируем строку для вывода информации о пользователе
    @Override
    public String toString() {
        return String.format("%s [%s]: %s - %.2f (%s)",
                date.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                type.equals("INCOME") ? "Доход" : "Расход",
                category, amount, description);
    }
}