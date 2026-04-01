package cn.gxust.player.fx.ui;

import javafx.application.Platform;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;

/**
 * Lightweight tray integration for the rebuilt JavaFX desktop client.
 */
public final class SystemTrayBridge {

    private final String applicationName;
    private TrayIcon trayIcon;

    public SystemTrayBridge(final String applicationName) {
        this.applicationName = applicationName;
    }

    public boolean isSupported() {
        return SystemTray.isSupported();
    }

    public void install(
        final Runnable showAction,
        final Runnable previousAction,
        final Runnable playPauseAction,
        final Runnable nextAction,
        final Runnable miniModeAction,
        final Runnable exitAction
    ) {
        if (!isSupported() || this.trayIcon != null) {
            return;
        }
        EventQueue.invokeLater(() -> {
            if (this.trayIcon != null) {
                return;
            }
            try {
                final PopupMenu popupMenu = new PopupMenu();
                popupMenu.add(menuItem("显示主窗口", showAction));
                popupMenu.add(menuItem("进入迷你模式", miniModeAction));
                popupMenu.addSeparator();
                popupMenu.add(menuItem("上一首", previousAction));
                popupMenu.add(menuItem("播放 / 暂停", playPauseAction));
                popupMenu.add(menuItem("下一首", nextAction));
                popupMenu.addSeparator();
                popupMenu.add(menuItem("退出", exitAction));

                this.trayIcon = new TrayIcon(createTrayImage(), this.applicationName, popupMenu);
                this.trayIcon.setImageAutoSize(true);
                this.trayIcon.addActionListener(event -> runFx(showAction));
                SystemTray.getSystemTray().add(this.trayIcon);
            } catch (Exception ignored) {
                this.trayIcon = null;
            }
        });
    }

    public void updateToolTip(final String currentSongTitle) {
        if (this.trayIcon == null) {
            return;
        }
        final String tooltip = currentSongTitle == null || currentSongTitle.isBlank()
            ? this.applicationName
            : this.applicationName + " - " + currentSongTitle;
        EventQueue.invokeLater(() -> this.trayIcon.setToolTip(tooltip));
    }

    public void remove() {
        if (this.trayIcon == null || !isSupported()) {
            return;
        }
        final TrayIcon iconToRemove = this.trayIcon;
        this.trayIcon = null;
        EventQueue.invokeLater(() -> SystemTray.getSystemTray().remove(iconToRemove));
    }

    private java.awt.MenuItem menuItem(final String label, final Runnable action) {
        final java.awt.MenuItem item = new java.awt.MenuItem(label);
        item.addActionListener(event -> runFx(action));
        return item;
    }

    private void runFx(final Runnable action) {
        Platform.runLater(() -> {
            if (action != null) {
                action.run();
            }
        });
    }

    private BufferedImage createTrayImage() {
        final BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(new Color(42, 66, 195));
        graphics.fillRoundRect(4, 4, 56, 56, 18, 18);
        graphics.setColor(new Color(102, 178, 255));
        graphics.fillOval(12, 12, 40, 40);
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("SansSerif", Font.BOLD, 28));
        graphics.drawString("♫", 20, 42);
        graphics.dispose();
        return image;
    }
}
