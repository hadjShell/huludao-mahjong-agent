package com.hadjshell.mahjong.domain.model.kong;

public sealed interface KongEvent permits DirectExposedKong, UpgradedKong, ConcealedKong {
}
