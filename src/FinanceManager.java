import java.io.*;
import java.util.*;

public class FinanceManager {
    private Map<String, User> users;
    private User currentUser;
    private static final String USERS_FILE = "users.dat";

    public FinanceManager() {
        this.users = new HashMap<>();
        loadUsers();

        //Этих двух типов я использовал при отладке, чтобы не создавать каждый раз заново
        //if (users.isEmpty()) {
        //    System.out.println("Созданы тестовые пользователи...");
        //    register("user1", "password1");
        //    register("user2", "password2");
        //}
    }

    //авторизация
    public boolean login(String username, String password) {
        User user = users.get(username);
        if (user != null && user.checkPassword(password)) {
            currentUser = user;
            System.out.println("Успешная авторизация! Добро пожаловать, " + username + "!");
            return true;
        } else {
            System.out.println("Ошибка авторизации: неверное имя пользователя или пароль.");
            return false;
        }
    }

    //регистрация
    public boolean register(String username, String password) {
        if (users.containsKey(username)) {
            System.out.println("Ошибка: пользователь с таким именем уже существует.");
            return false;
        }

        User newUser = new User(username, password);
        users.put(username, newUser);
        saveUsers(); //сохраняем обновленный список пользователей
        System.out.println("Пользователь " + username + " успешно зарегистрирован!");
        return true;
    }

    //сохранение списка пользователей
    private void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            //сохраняем только базовую информацию о пользователях (логины и пароли)
            Map<String, String> userCredentials = new HashMap<>();
            for (Map.Entry<String, User> entry : users.entrySet()) {
                userCredentials.put(entry.getKey(), entry.getValue().getPasswordHash());
            }
            oos.writeObject(userCredentials);
            System.out.println("Список пользователей сохранен.");
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении списка пользователей: " + e.getMessage());
        }
    }

    //загрузка списка пользователей
    @SuppressWarnings("unchecked")
    private void loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            System.out.println("Файл пользователей не найден. Будет создан новый.");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USERS_FILE))) {
            Map<String, String> userCredentials = (Map<String, String>) ois.readObject();

            for (Map.Entry<String, String> entry : userCredentials.entrySet()) {
                User user = new User(entry.getKey(), entry.getValue());
                users.put(entry.getKey(), user);
            }
            System.out.println("Список пользователей загружен. Найдено пользователей: " + users.size());
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Ошибка при загрузке списка пользователей: " + e.getMessage());
        }
    }

    //перевод между пользователями
    public boolean transfer(String receiverUsername, double amount, String description) {
        if (currentUser == null) {
            System.out.println("Ошибка: необходимо авторизоваться.");
            return false;
        }

        User receiver = users.get(receiverUsername);
        if (receiver == null) {
            System.out.println("Ошибка: получатель не найден.");
            return false;
        }

        if (currentUser.getUsername().equals(receiverUsername)) {
            System.out.println("Ошибка: нельзя переводить самому себе.");
            return false;
        }

        Wallet senderWallet = currentUser.getWallet();
        Wallet receiverWallet = receiver.getWallet();

        //проверяем баланс отправителя
        if (senderWallet.getBalance() < amount) {
            System.out.println("Ошибка: недостаточно средств для перевода.");
            return false;
        }

        //выполняем перевод
        senderWallet.addExpense("Перевод", amount, "Перевод пользователю " + receiverUsername);
        receiverWallet.addIncome("Перевод", amount, "Перевод от пользователя " + currentUser.getUsername());

        System.out.println("Перевод успешно выполнен!");
        System.out.println("Отправитель: " + currentUser.getUsername() + ", Сумма: " + amount);
        System.out.println("Получатель: " + receiverUsername);

        //сохраняем изменения в файлах пользователей
        currentUser.saveData();
        receiver.saveData();

        return true;
    }

    //выход из системы
    public void logout() {
        if (currentUser != null) {
            try {
                currentUser.saveData();
                System.out.println("Данные сохранены. До свидания, " + currentUser.getUsername() + "!");
            } catch (Exception e) {
                System.out.println("Ошибка при сохранении данных: " + e.getMessage());
            } finally {
                currentUser = null;
            }
        }
    }

    //сохранение всех данных при завершении работы
    public void saveAllData() {
        System.out.println("Сохранение всех данных...");
        for (User user : users.values()) {
            user.saveData();
        }
        saveUsers();
        System.out.println("Все данные сохранены.");
    }

    //геттеры
    public User getCurrentUser() { return currentUser; }
    public Map<String, User> getUsers() { return users; }
}