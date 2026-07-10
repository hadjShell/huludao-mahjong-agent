package com.hadjshell.mahjong.domain.settlement;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.hadjshell.mahjong.domain.model.Seat;
import com.hadjshell.mahjong.domain.model.report.KongBreakdown;
import com.hadjshell.mahjong.domain.model.report.LostBreakdown;

public record SettlementResult(
    Map<Seat, Integer>            winDelta,
    Map<Seat, Integer>            kongDelta,
    Map<Seat, Integer>            totalDelta,
    Map<Seat, BigDecimal>         moneyDelta,
    List<LostBreakdown>           lostBreakdowns,
    List<KongBreakdown>           kongBreakdowns
) {
  public SettlementResult {
    Objects.requireNonNull(winDelta, "winDelta must not be null");
    Objects.requireNonNull(kongDelta, "kongDelta must not be null");
    Objects.requireNonNull(totalDelta, "totalDelta must not be null");
    Objects.requireNonNull(moneyDelta, "moneyDelta must not be null");
    Objects.requireNonNull(lostBreakdowns, "LostBreakdown must not be null");
    Objects.requireNonNull(kongBreakdowns, "kongBreakdowns must not be null");

    validateSeatMap(winDelta, "winDelta");
    validateSeatMap(kongDelta, "kongDelta");
    validateSeatMap(totalDelta, "totalDelta");
    validateSeatMap(moneyDelta, "moneyDelta");

    winDelta = Map.copyOf(winDelta);
    kongDelta = Map.copyOf(kongDelta);
    totalDelta = Map.copyOf(totalDelta);
    moneyDelta = Map.copyOf(moneyDelta);
    lostBreakdowns = List.copyOf(lostBreakdowns);
    kongBreakdowns = List.copyOf(kongBreakdowns);
  }

  private static <T> void validateSeatMap(Map<Seat, T> map, String name) {
    for (Seat seat : Seat.values()) {
      if (!map.containsKey(seat)) {
        throw new IllegalArgumentException(name + " must contain every seat");
      }
      Objects.requireNonNull(map.get(seat), name + " values must not be null");
    }
  }
}
