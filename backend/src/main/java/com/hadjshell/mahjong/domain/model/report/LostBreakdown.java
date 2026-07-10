package com.hadjshell.mahjong.domain.model.report;

import java.util.List;
import java.util.Objects;

import com.hadjshell.mahjong.domain.model.Seat;
import com.hadjshell.mahjong.domain.model.SettlementReason;

public record LostBreakdown(
    Seat                    loser,
    int                     lostFan,
    List<SettlementReason>  lostReasons
) {
  public LostBreakdown {
    Objects.requireNonNull(loser, "loser must not be null");
    if (lostFan < 0)
      throw new IllegalArgumentException("lostFan in LostBreakdown must not be negative");
    Objects.requireNonNull(lostReasons, "lostReasons must not be null");
    
    lostReasons = List.copyOf(lostReasons);
  }
}
