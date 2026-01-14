import java.io.*;
import java.util.*;

public class Wallet implements Serializable {
    //поля
    private static final long serialVersionUID = 1L;
    private String userId;                 //владелец кошелька
    private double balance;                //текущий баланс
    private List<Transaction> transactions;//история транзакций
    private List<Budget> budgets;          //ограничения по категориям
    private Set<String> categories;        //собственно заявленные категории
    //конструктор
    public Wallet(String userId) {
        this.userId = userId;                 //устанавливаем владельца
        this.balance = 0.0;                   //инициализируем баланс в zero
        this.transactions = new ArrayList<>();//создаем коллекции транзакций, лимитов и категорий
        this.budgets = new ArrayList<>();
        this.categories = new HashSet<>();
    }

    //метод для добавления дохода
    public void addIncome(String category, double amount, String description) {
        //создаем транзакцию
        Transaction income = new Transaction("INCOME", category, amount, description, userId);
        //фиксируем ее для потомков
        transactions.add(income);
        //обновляем баланс
        balance += amount;
        //добавляем категорию
        categories.add(category);
        //уведомляем пользователя
        System.out.println("Доход добавлен: " + category + " - " + amount);
    }

    //метод для добавления расхода
    public boolean addExpense(String category, double amount, String description) {
        // Проверка на отрицательный баланс
        if (balance - amount < 0) {
            System.out.println("Ошибка: недостаточно средств! Текущий баланс: " + balance);
            return false;
        }

        Transaction expense = new Transaction("EXPENSE", category, amount, description, userId);
        transactions.add(expense);
        balance -= amount;
        categories.add(category);

        //проверка превышения бюджета (кроме всего описанного в добавлении дохода здесь надо проверить и это)
        checkBudgetExceeded(category, amount);

        System.out.println("Расход добавлен: " + category + " - " + amount);
        return true;
    }

    //метод для установки бюджета
    public void setBudget(String category, double limit) {
        //ищем существующий бюджет
        for (Budget budget : budgets) {
            if (budget.getCategory().equals(category)) {
                budget.setLimit(limit);  //обновляем существующий лимит
                System.out.println("Бюджет обновлен для категории: " + category + " - " + limit);
                return;
            }
        }

        //создаем новый бюджет
        Budget newBudget = new Budget(category, limit, userId);
        budgets.add(newBudget);
        categories.add(category);
        System.out.println("Бюджет установлен для категории: " + category + " - " + limit);
    }

    //проверка превышения бюджета
    private void checkBudgetExceeded(String category, double amount) {
        for (Budget budget : budgets) {
            if (budget.getCategory().equals(category)) {
                double spent = getExpensesByCategory(category);
                double remaining = budget.getLimit() - spent;

                if (remaining < 0) {
                    System.out.println("ВНИМАНИЕ: Превышен бюджет в категории '" + category +
                            "'! Превышение: " + Math.abs(remaining));
                } else if (remaining < budget.getLimit() * 0.1) {
                    System.out.println("ВНИМАНИЕ: Бюджет в категории '" + category +
                            "' почти исчерпан! Осталось: " + remaining);
                }
                break;
            }
        }
    }

    //получение расходов по категории
    public double getExpensesByCategory(String category) {
        return transactions.stream()
                .filter(t -> t.getType().equals("EXPENSE") && t.getCategory().equals(category)) //фильтруем расходы по категориям
                .mapToDouble(Transaction::getAmount)                                            //Преобразуем в поток сумм и суммируем
                .sum();
    }

    //получение доходов по категории
    public double getIncomeByCategory(String category) {
        return transactions.stream()
                .filter(t -> t.getType().equals("INCOME") && t.getCategory().equals(category))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    //общие расходы
    public double getTotalExpenses() {
        return transactions.stream()
                .filter(t -> t.getType().equals("EXPENSE")) //фильтрует транзакции типа EXPENSE
                .mapToDouble(Transaction::getAmount)        //Преобразуем в поток сумм и суммируем его (сложность O(n))
                .sum();
    }

    //общие доходы
    public double getTotalIncome() {
        return transactions.stream()
                .filter(t -> t.getType().equals("INCOME"))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    //расчет по нескольким категориям (позволяет сравнить траты, найти на чем сэкономить и т.п.)
    public Map<String, Double> calculateByCategories(List<String> categoriesToCalculate) {
        Map<String, Double> result = new HashMap<>();

        for (String category : categoriesToCalculate) {
            if (!categories.contains(category)) {
                System.out.println("Категория '" + category + "' не найдена!");//проверка существования категории
                continue;
            }

            double expenses = getExpensesByCategory(category);
            double income = getIncomeByCategory(category);
            result.put(category + "_расходы", expenses);
            result.put(category + "_доходы", income);
        }

        return result;
    }

    //получение информации о бюджетах
    public void displayBudgetInfo() {
        System.out.println("\n=== Информация о бюджетах ===");
        if (budgets.isEmpty()) {
            System.out.println("Бюджеты не установлены.");
            return;
        }

        for (Budget budget : budgets) {
            String category = budget.getCategory();
            double limit = budget.getLimit();
            double spent = getExpensesByCategory(category);
            double remaining = limit - spent;

            System.out.printf("%s: Лимит: %.2f, Потрачено: %.2f, Осталось: %.2f%n",
                    category, limit, spent, remaining);
        }
    }

    //сохранение кошелька в файл
    public void saveToFile() {
        try {
            // Создаем директорию wallets, если она не существует
            File walletsDir = new File("wallets");
            if (!walletsDir.exists()) {
                boolean created = walletsDir.mkdir();
                if (!created) {
                    System.out.println("Не удалось создать директорию для сохранения данных.");
                    return;
                }
            }

            //создаем безопасное имя файла
            String safeUsername = userId.replaceAll("[^a-zA-Z0-9._-]", "_");
            String filepath = "wallets/wallet_" + safeUsername + ".dat";

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filepath))) {
                oos.writeObject(this);
                System.out.println("Данные успешно сохранены в файл: " + filepath);
            }
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении данных: " + e.getMessage());
            // Для отладки можно раскомментировать следующую строку:
            // e.printStackTrace();
        }
    }

    //загрузка кошелька из файла
    public static Wallet loadFromFile(String userId) {
        try {
            //создаем безопасное имя файла
            String safeUsername = userId.replaceAll("[^a-zA-Z0-9._-]", "_");
            String filepath = "wallets/wallet_" + safeUsername + ".dat";

            File file = new File(filepath);

            if (!file.exists()) {
                System.out.println("Файл данных не найден. Создан новый кошелек.");
                return new Wallet(userId);
            }

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filepath))) {
                Wallet wallet = (Wallet) ois.readObject();
                System.out.println("Данные успешно загружены из файла: " + filepath);
                return wallet;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Ошибка при загрузке данных: " + e.getMessage());
            System.out.println("Создан новый кошелек.");
            return new Wallet(userId);
        }
    }

    //геттеры
    public String getUserId() { return userId; }
    public double getBalance() { return balance; }
    public List<Transaction> getTransactions() { return transactions; }
    public List<Budget> getBudgets() { return budgets; }
    public Set<String> getCategories() { return categories; }


    //метод для отображения истории транзакций
    public void displayTransactions() {
        System.out.println("\n=== История транзакций ===");
        if (transactions.isEmpty()) {
            System.out.println("Транзакций нет.");
            return;
        }

        //сортируем транзакции по дате (самые новые первыми)
        transactions.sort((t1, t2) -> t2.getDate().compareTo(t1.getDate()));

        System.out.println("Всего транзакций: " + transactions.size());
        System.out.println(String.format("%-25s %-10s %-20s %-10s %s",
                "Дата", "Тип", "Категория", "Сумма", "Описание"));
        System.out.println("-------------------------------------------------------------------------------");

        for (Transaction t : transactions) {
            String typeSymbol = t.getType().equals("INCOME") ? "+" : "-";
            String formattedDate = t.getDate().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

            System.out.println(String.format("%-25s %-10s %-20s %-10.2f %s",
                    formattedDate,
                    t.getType().equals("INCOME") ? "Доход" : "Расход",
                    t.getCategory(),
                    t.getAmount(),
                    t.getDescription()));
        }

        System.out.println("-------------------------------------------------------------------------------");
        System.out.printf("Итого доходов: %.2f | Итого расходов: %.2f | Баланс: %.2f%n",
                getTotalIncome(), getTotalExpenses(), balance);
    }
}