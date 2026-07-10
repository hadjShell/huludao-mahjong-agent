package com.hadjshell.mahjong.domain.model.report;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.hadjshell.mahjong.domain.model.Seat;
import com.hadjshell.mahjong.domain.model.SettlementReason;
import com.hadjshell.mahjong.domain.model.kong.KongEvent;

public record KongBreakdown(
    KongEvent event,
    Map<Seat, Integer> kongDelta,
    List<SettlementReason> reasons
) {
  public KongBreakdown {  
    Objects.requireNonNull(event, "event must not be null");
    Objects.requireNonNull(kongDelta, "kongDelta must not be null");
    Objects.requireNonNull(reasons, "reasons must not be null");

    for (Seat seat : Seat.values()) {
      if (!kongDelta.containsKey(seat)) {
        throw new IllegalArgumentException("kongDelta must contain every seat");
      }
      Objects.requireNonNull(kongDelta.get(seat), "kongDelta values must not be null");
    }

    kongDelta = Map.copyOf(kongDelta);
    reasons = List.copyOf(reasons);
  }
}
