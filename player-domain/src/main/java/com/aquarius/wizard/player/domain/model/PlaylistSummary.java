package com.aquarius.wizard.player.domain.model;

/**
 * Lightweight playlist summary for the rebuilt desktop landing page.
 *
 * @param title       playlist title
 * @param description short description
 * @param accentColor playlist accent color
 */
public record PlaylistSummary(String title, String description, String accentColor) {
}

