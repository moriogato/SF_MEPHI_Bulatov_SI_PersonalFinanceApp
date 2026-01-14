public class Budget {
    //поля
    private String category;
    private double limit;
    private String userId;
    //конструктор
    public Budget(String category, double limit, String userId) {
        this.category = category;
        this.limit = limit;
        this.userId = userId;
    }

    //геттеры и сеттеры
    public String getCategory() { return category; }           //получить категорию
    public double getLimit() { return limit; }                 //получить лимит
    public String getUserId() { return userId; }               //получить имя пользователя
    public void setLimit(double limit) { this.limit = limit; } //изменить лимит
}
//реализация бюджета самая простая - сумма без учета времени, было много работы, я это кое-как отладил к дедлайну
//Но! Сериализация позволяет бюджет сохранять вместе с кошельком и загружать при следующем входе.