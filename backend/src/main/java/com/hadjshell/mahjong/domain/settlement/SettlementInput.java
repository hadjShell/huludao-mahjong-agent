package com.hadjshell.mahjong.domain.settlement;

import java.util.List;
import java.util.Objects;

import com.hadjshell.mahjong.domain.config.GameConfig;
import com.hadjshell.mahjong.domain.model.Seat;
import com.hadjshell.mahjong.domain.model.kong.KongEvent;
import com.hadjshell.mahjong.domain.model.win.WinOutcome;

public record SettlementInput(
    GameConfig                  config,
    Seat                        dealer,
    WinOutcome                  winOutcome,
    List<KongEvent>             kongEvents
) {
  public SettlementInput {
    Objects.requireNonNull(config, "config must not be null");
    Objects.requireNonNull(dealer, "dealer must not be null");
    Objects.requireNonNull(winOutcome, "winOutcome must not be null");
    Objects.requireNonNull(kongEvents, "kongEvents must not be null");
    kongEvents = List.copyOf(kongEvents);
  }
}


