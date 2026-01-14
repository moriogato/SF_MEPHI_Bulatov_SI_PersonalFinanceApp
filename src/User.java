import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private String password;
    private transient Wallet wallet; //transient - не сериализуем кошелек

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        //кошелек загружается только при первом обращении
    }

    //инициализация кошелька (если пользователь зашел и сразу вышел, то кошелек не грузится лишний раз)
    public Wallet getWallet() {
        if (wallet == null) {
            wallet = Wallet.loadFromFile(username);
        }
        return wallet;
    }

    //проверка пароля
    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    //геттер для хеша пароля (для сохранения в файле)
    public String getPasswordHash() {
        //в реальном приложении здесь может быть хеширование пароля, но и без пароля все очень разрослось
        return password;
    }

    //геттеры
    public String getUsername() { return username; }

    //сохранение данных пользователя
    public void saveData() {
        if (wallet != null) {
            wallet.saveToFile();
        }
    }

    @Override
    public String toString() {
        return "User{" + username + "}";
    }
}