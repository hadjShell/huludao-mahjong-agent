package com.hadjshell.mahjong.domain.model.win;

import java.util.Map;
import java.util.Objects;

import com.hadjshell.mahjong.domain.model.Seat;

public record SelfDrawWin(
    Seat winner,
    WinningCategory category,
    Map<Seat, HandVisibility> loserVisibility,
    boolean isKongReplacementWin
) implements WinOutcome {
  public SelfDrawWin {
    Objects.requireNonNull(winner, "winner must not be null");
    Objects.requireNonNull(category, "category must not be null");
    Objects.requireNonNull(loserVisibility, "loserVisibility must not be null");
    validateLoserVisibility(loserVisibility);

    if (loserVisibility.size() != 3)
      throw new IllegalArgumentException("loserVisibility must contain three losers");
    if (loserVisibility.containsKey(winner))
      throw new IllegalArgumentException("winner must not be in loserVisibility");
    loserVisibility = Map.copyOf(loserVisibility);
  }

  private static void validateLoserVisibility(Map<Seat, HandVisibility> loserVisibility) {
    for (Map.Entry<Seat, HandVisibility> entry : loserVisibility.entrySet()) {
      Objects.requireNonNull(entry.getKey(), "loserVisibility seats must not be null");
      Objects.requireNonNull(entry.getValue(), "loserVisibility values must not be null");
    }
  }
}
