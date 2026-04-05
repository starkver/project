import com.rogue.presentation.GameView;
import com.rogue.presentation.MenuView;
import com.rogue.data.SaveManager;
import com.rogue.domain.GameSession;

public class Main {
    public static void main(String[] args) {
        MenuView menu = new MenuView();
        boolean continueGame = menu.showMainMenu();

        GameSession session;
        SaveManager saveManager = new SaveManager();

        if (continueGame && saveManager.hasSaveGame()) {
            session = saveManager.loadGame();
            if (session == null) {
                session = new GameSession();
            }
        } else {
            session = new GameSession();
        }

        GameView gameView = new GameView(session);
        gameView.start();
    }
}