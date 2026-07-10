package com.hadjshell.mahjong.domain.model.win;

public sealed interface WinOutcome permits 
    DiscardWin, SelfDrawWin, ExhaustiveDraw {
}
