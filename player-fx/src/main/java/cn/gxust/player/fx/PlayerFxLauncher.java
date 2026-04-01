package cn.gxust.player.fx;

/**
 * Separate launcher avoids the "JavaFX runtime components are missing" issue
 * that can occur when the main class directly extends {@code Application}.
 */
public final class PlayerFxLauncher {

    private PlayerFxLauncher() {
    }

    public static void main(final String[] args) {
        PlayerFxApplication.launchStandalone();
    }
}
