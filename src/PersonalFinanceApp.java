import java.util.*; //Утилиты (Сканер, листы, мапа и пр)

public class PersonalFinanceApp {
    private static FinanceManager financeManager;
    private static Scanner scanner;                   //Чтение ввода пользователя, статический потому что используется в статических методах

    public static void main(String[] args) { //Инициируем компоненты
        financeManager = new FinanceManager();
        scanner = new Scanner(System.in);
        //Приветственная строка
        System.out.println("=== Система управления личными финансами ===");
        // Добавляем обработчик завершения приложения, так как данные сохраняются при выходе и просто бросить его нельзя
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nЗавершение работы приложения...");
            financeManager.saveAllData();
            scanner.close();
        }));

        // Главный цикл приложения
        while (true) {
            if (financeManager.getCurrentUser() == null) {
                showAuthMenu();
            } else {
                showMainMenu();
            }
        }
    }

    // Меню авторизации, показываем его в главном цикле
    private static void showAuthMenu() {
        System.out.println("\n=== Меню авторизации ===");
        System.out.println("Войти - нажмите 1");
        System.out.println("Зарегистрироваться - нажмите 2");
        System.out.println("Показать список пользователей (админ) - нажмите 3");
        System.out.println("Выйти из приложения - нажмите 4");
        System.out.print("Выберите действие: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine()); //Считываем выбор пользователя

            switch (choice) {
                case 1:
                    login();
                    break;
                case 2:
                    register();
                    break;
                case 3:
                    showUsersList();
                    break;
                case 4:
                    System.out.println("Сохранение данных и выход из приложения...");
                    financeManager.saveAllData();
                    scanner.close();
                    System.exit(0);
                    break;
                default:
                    System.out.println("Неверный выбор. Попробуйте снова.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: введите число!");
        }
    }

    // Метод для отображения списка пользователей
    private static void showUsersList() {
        System.out.println("\n=== Список зарегистрированных пользователей ===");
        Map<String, User> users = financeManager.getUsers();

        if (users.isEmpty()) {
            System.out.println("Пользователи не найдены.");
            return;
        }
        //Считаем пользователей и возвращаем их имена, чтобы вывести списком
        int i = 1;
        for (String username : users.keySet()) { //Достаем Имя
            System.out.println(i + ". " + username); //Выводим Номер пользователя. Имя пользователя
            i++;
        }
        System.out.println("Всего пользователей: " + users.size());
    }

    // Метод авторизации
    private static void login() {
        System.out.print("Введите имя пользователя: ");
        String username = scanner.nextLine();
        System.out.print("Введите пароль: ");
        String password = scanner.nextLine();

        financeManager.login(username, password); //и запоминаем. Проект учебный, заморачиваться с защитой данных я не стал
    }

    // Метод регистрации
    private static void register() {
        System.out.print("Введите имя пользователя: ");
        String username = scanner.nextLine();
        System.out.print("Введите пароль: ");
        String password = scanner.nextLine();

        financeManager.register(username, password);
    }

    // Главное меню для пользователя
    private static void showMainMenu() {
        User currentUser = financeManager.getCurrentUser(); //Получаем сведения о текущем пользователе
        Wallet wallet = currentUser.getWallet();

        System.out.println("\n=== Главное меню ===");
        System.out.println("Текущий пользователь: " + currentUser.getUsername());
        System.out.printf("Текущий баланс: %.2f%n", wallet.getBalance());
        System.out.println("Добавить доход - нажмите 1");
        System.out.println("Добавить расход - нажмите 2");
        System.out.println("Установить/изменить бюджет - нажмите 3");
        System.out.println("Показать статистику - нажмите 4");
        System.out.println("Показать информацию о бюджетах - нажмите 5");
        System.out.println("Показать историю транзакций - нажмите 6");
        System.out.println("Перевести средства другому пользователю - нажмите 7");
        System.out.println("Сохранить данные - нажмите 8");
        System.out.println("Выйти из системы - нажмите 9");
        System.out.println("Сохранить и выйти из приложения - нажмите 0");
        System.out.print("Выберите действие: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    addIncome();
                    break;
                case 2:
                    addExpense();
                    break;
                case 3:
                    setBudget();
                    break;
                case 4:
                    showStatistics();
                    break;
                case 5:
                    wallet.displayBudgetInfo();
                    break;
                case 6:
                    wallet.displayTransactions();
                    break;
                case 7:
                    makeTransfer();
                    break;
                case 8:
                    currentUser.saveData();
                    System.out.println("Данные сохранены.");
                    break;
                case 9:
                    financeManager.logout();
                    break;
                case 0:
                    financeManager.saveAllData();
                    System.out.println("Данные сохранены. Выход из приложения...");
                    scanner.close();
                    System.exit(0);
                    break;
                default:
                    System.out.println("Неверный выбор. Попробуйте снова.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: введите число!");
        } catch (Exception e) {
            System.out.println("Произошла ошибка: " + e.getMessage());
        }
    }

    // Получаем вводные данные от пользователя
    private static void addIncome() {
        System.out.print("Введите категорию дохода: ");
        String category = scanner.nextLine();
        System.out.print("Введите сумму: ");
        double amount = getValidDouble();
        System.out.print("Введите описание: ");
        String description = scanner.nextLine();
        //Попробовал цепочку вызовов - получили сведения о пользователе, добавили доход
        financeManager.getCurrentUser().getWallet().addIncome(category, amount, description);
    }

    private static void addExpense() {
        System.out.print("Введите категорию расхода: ");
        String category = scanner.nextLine();
        System.out.print("Введите сумму: ");
        double amount = getValidDouble();
        System.out.print("Введите описание: ");
        String description = scanner.nextLine();
        //Соответственно добавили расход к пользователю
        financeManager.getCurrentUser().getWallet().addExpense(category, amount, description);
    }

    private static void setBudget() {
        System.out.print("Введите категорию для бюджета: ");
        String category = scanner.nextLine();
        System.out.print("Введите лимит бюджета: ");
        double limit = getValidDouble();

        financeManager.getCurrentUser().getWallet().setBudget(category, limit);
    }

    private static void showStatistics() {
        Wallet wallet = financeManager.getCurrentUser().getWallet();

        System.out.println("\n=== Статистика ===");
        System.out.printf("Общий доход: %.2f%n", wallet.getTotalIncome()); //два знака после запятой
        System.out.printf("Общие расходы: %.2f%n", wallet.getTotalExpenses());
        System.out.printf("Текущий баланс: %.2f%n", wallet.getBalance());

        if (wallet.getTotalExpenses() > wallet.getTotalIncome()) {
            System.out.println("ВНИМАНИЕ: Расходы превышают доходы!");
        }

        System.out.println("\nДоходы по категориям:");
        for (String category : wallet.getCategories()) {
            double income = wallet.getIncomeByCategory(category);
            if (income > 0) {
                System.out.printf("%s: %.2f%n", category, income);
            }
        }

        System.out.println("\nРасходы по категориям:");
        for (String category : wallet.getCategories()) {
            double expense = wallet.getExpensesByCategory(category);
            if (expense > 0) {
                System.out.printf("%s: %.2f%n", category, expense);
            }
        }
    }
    //Аналогично берем информацию по переводам другим пользователям
    private static void makeTransfer() {
        System.out.print("Введите имя получателя: ");
        String receiver = scanner.nextLine();
        System.out.print("Введите сумму перевода: ");
        double amount = getValidDouble();
        System.out.print("Введите описание: ");
        String description = scanner.nextLine();

        financeManager.transfer(receiver, amount, description);
    }
    //Валидация введеных сумм
    private static double getValidDouble() {
        while (true) {
            try {
                String input = scanner.nextLine();
                double value = Double.parseDouble(input);
                if (value <= 0) {
                    System.out.print("Ошибка: сумма должна быть положительной. Введите снова: ");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.print("Ошибка: введите корректное число: ");
            }
        }
    }
}