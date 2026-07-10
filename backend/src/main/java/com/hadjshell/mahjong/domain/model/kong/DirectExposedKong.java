package com.hadjshell.mahjong.domain.model.kong;

import com.hadjshell.mahjong.domain.model.Seat;
import java.util.Objects;

public record DirectExposedKong(
    Seat declarer,
    Seat discarder,
    KongTileType tileType
) implements KongEvent {
  public DirectExposedKong {
    Objects.requireNonNull(declarer, "declarer must not be null");
    Objects.requireNonNull(discarder, "discarder must not be null");
    Objects.requireNonNull(tileType, "tileType must not be null");

    if (declarer.equals(discarder))
      throw new IllegalArgumentException("Kong declarer and discarder must not be the same player");
  }

  public int fan() {
    return tileType == KongTileType.DRAGON ? 4 : 2;
  }
}
