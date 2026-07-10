package com.hadjshell.mahjong.domain.config;

import com.hadjshell.mahjong.domain.model.Seat;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record GameConfig(
    BigDecimal base,
    int initialPoints,
    int roundCount,
    List<Seat> seatOrder
) {
  public static final BigDecimal DEFAULT_BASE = BigDecimal.ONE;
  public static final int DEFAULT_INITIAL_POINTS = 100;
  public static final int DEFAULT_ROUND_COUNT = 10;

  public GameConfig {
    Objects.requireNonNull(base, "base must not be null");
    Objects.requireNonNull(seatOrder, "seatOrder must not be null");
    if (base.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("base must be positive");
    }
    if (initialPoints <= 0) {
      throw new IllegalArgumentException("initialPoints must be positive");
    }
    if (roundCount <= 0) {
      throw new IllegalArgumentException("roundCount must be positive");
    }
    if (!seatOrder.containsAll(Seat.defaultOrder()) || seatOrder.size() != Seat.defaultOrder().size()) {
      throw new IllegalArgumentException("seatOrder must contain exactly the four seats");
    }
    seatOrder = List.copyOf(seatOrder);
  }

  public static GameConfig defaultConfig() {
    return new GameConfig(
        DEFAULT_BASE,
        DEFAULT_INITIAL_POINTS,
        DEFAULT_ROUND_COUNT,
        Seat.defaultOrder()
    );
  }
}
