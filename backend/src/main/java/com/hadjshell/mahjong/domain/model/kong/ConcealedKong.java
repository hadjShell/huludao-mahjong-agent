package com.hadjshell.mahjong.domain.model.kong;

import com.hadjshell.mahjong.domain.model.Seat;
import java.util.Objects;

public record ConcealedKong(
    Seat declarer,
    KongTileType tileType
) implements KongEvent {
  public ConcealedKong {
    Objects.requireNonNull(declarer, "declarer must not be null");
    Objects.requireNonNull(tileType, "tileType must not be null");
  }

  public int fan() {
    return tileType == KongTileType.DRAGON ? 8 : 4;
  }
}
