package com.aquarius.wizard.player.domain.model;

import java.time.Duration;

/**
 * A single timed lyric line.
 *
 * @param position timeline position
 * @param content  lyric text
 */
public record LyricLine(Duration position, String content) {
}

