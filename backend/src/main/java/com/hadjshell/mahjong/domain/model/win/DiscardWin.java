package com.hadjshell.mahjong.domain.model.win;

import java.util.Map;
import java.util.Objects;

import com.hadjshell.mahjong.domain.model.Seat;

public record DiscardWin(
    Seat winner,
    Seat discarder,
    WinningCategory category,
    Map<Seat, HandVisibility> loserVisibility
) implements WinOutcome {
  public DiscardWin {
    Objects.requireNonNull(winner, "winner must not be null");
    Objects.requireNonNull(discarder, "discarder must not be null");
    Objects.requireNonNull(category, "category must not be null");
    Objects.requireNonNull(loserVisibility, "loserVisibility must not be null");
    validateLoserVisibility(loserVisibility);

    if (loserVisibility.size() != 3)
      throw new IllegalArgumentException("loserVisibility must contain three losers");
    if (discarder.equals(winner))
      throw new IllegalArgumentException("discarder must not be winner");
    if (loserVisibility.containsKey(winner))
      throw new IllegalArgumentException("winner must not be in loserVisibility");
    if (!loserVisibility.containsKey(discarder))
      throw new IllegalArgumentException("discarder must be in loserVisibility");
    loserVisibility = Map.copyOf(loserVisibility);
  }

  private static void validateLoserVisibility(Map<Seat, HandVisibility> loserVisibility) {
    for (Map.Entry<Seat, HandVisibility> entry : loserVisibility.entrySet()) {
      Objects.requireNonNull(entry.getKey(), "loserVisibility seats must not be null");
      Objects.requireNonNull(entry.getValue(), "loserVisibility values must not be null");
    }
  }
}
