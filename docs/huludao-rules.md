# Huludao Mahjong Rules

This is the source of truth for the rule engine.

## Tile Notation

Use a compact notation consistently in examples.

- Characters: `1w` to `9w`
- Bamboos: `1t` to `9t`
- Dots: `1p` to `9p`
- Winds: `E`, `S`, `W`, `N`
- Dragons: `Z`, `F`, `B`
- Each tile has 4 copies. In total 136 tiles.

## Terminology

### Basic Actions

- Tile: The "card" in mahjong.
- Yaojiu tile: 1, 9, wind, or dragon tile.
- Seat: The player’s position at the table for the game.
- Draw a tile: To take a tile from the wall on your turn.
- Discard a tile: To put one tile out after drawing.
- Hand: The tiles you currently hold. Hand includes three areas: hand area, meld area, concealed kong area.
- Hand area: The area where the player’s private tiles are placed.
- Meld area: The area where exposed combinations are placed after a player calls chow, pong, or exposed kong.
- Concealed kong area: The area for concealed kong.
- Winning tile: The exact tile that completes your hand and allows you to win.
- Wait tiles: All possible tiles that can complete your hand.
- Wall: The stack of face-down tiles players draw from.

### Players and Round Roles

- Dealer: The main player of the round. Dealer has special rules.
- Non-dealer: Any player who is not the dealer.

### Making Sets

- Chow: To use another player’s discarded tile to form a sequence, only from the player before you. For example, “I can chow the 4 bamboo to make 2-3-4 bamboo.”
- Pong: To use another player’s discarded tile to form three identical tiles. For example, “She ponged my Red Dragon.”
- Kong: To form four identical tiles.
- Exposed kong: A kong shown openly to everyone, usually made using another player’s discard or upgraded from an exposed pong.
- Concealed kong: A kong made from four tiles in your own hand, not from another player’s discard.

### Hand Structure

- Sequence: Three consecutive tiles in the same suit, such as 2-3-4 bamboo.
- Triplet: Three identical tiles.
- Pair: Two identical tiles.

### Winning and Waiting

- Wait: Your hand is one tile away from winning. For example, “I’m waiting on the 6 bamboo.”
- Win: To complete a legal winning hand.
- Self-draw win: You win by drawing the winning tile yourself.
- Win on discard: Another player discards the tile you need, and you win on it.
- Exhaust: No one wins before the wall runs out. For example, “The round ended in an exhaustive draw.”

### Scoring

- Fan: A unit used to calculate the value of a hand.
- Base: The amount of money bound to one fan.

## Game Configuration

- Number of players: 4
- Number of dealer: 1
- Number of non-dealer: 3
- Round: 10 by default.
- Initial points: 100 by default.
- Start seat: East.
- Dealer behavior
  - If the dealer wins, he remains the dealer, otherwise his next player becomes the dealer (clockwise). For example, if East wins, East remains the dealer in the next round; if East doesn't win, North becomes the dealer for the next round.
  - Clockwise seat order is East -> North -> West -> South -> East.
- Base: 1 rmb per fan by default.

## Winning Hand Rules

### Rules

- The completed winning hand must include at least one character tile, one bamboo tile, and one dot tile.
- The completed winning hand must include at least one yaojiu tile: 1, 9, wind, or dragon.
- All winning hands must be open except Seven pairs. All triplets is not exempt from the open-hand requirement.
- You must have at least one sequence in your hand, either in the hand area or the meld area. But if you have a pair of any dragon tile, e.g. `Z Z`, this rule can be excluded.
- Your hand area cannot only have one tile left, except for all triplets.
- If you pong dragon tiles, you can only win all triplets. But if you kong dragon tiles, you are allowed to win other types.

### Winning Categories

- Basic win: `n * sequence + m * triplet + pair, n + m = 4, n >= 1, m >= 0`. For example, 2-3-4 bamboo, 3-4-5 dots, 7-8-9 characters, 4-5-6 bamboo, pair of 6 dots.

- Single win: a basic win under the following scenarios:
  - Edge wait: Waiting on 3 to complete 1-2-3, or waiting on 7 to complete 7-8-9. For example, you have 1-2 bamboo, waiting for 3 bamboo.
  - Close wait: Waiting for the middle tile of a sequence. For example, you have 2-4 dots, waiting for 3 dots.
  - Pair wait: Waiting for one tile to complete your pair. For example, you have one Red Dragon, waiting for another Red Dragon.
  - Yaojiu tile wait: Your only winning tile is a 1, 9, wind, or dragon tile. For example, You are only waiting for 9 bamboo or East wind.

- Seven pairs: A special hand made of seven pairs. Four identical tiles can count as two pairs. Your winning hand is not open. For example, Pair of 1 dots, pair of 3 dots, pair of Easts, pair of 5 bamboos, pair of 6 bamboos, pair of 6 characters, pair of 9 characters.

- All triplets: Your hand is made of triplets/kongs plus one pair.

- Higher category wins. Seven pairs and All triplets override Single/Basic. Single overrides Basic.

## Settlement Rules

Define settlement as two separate calculations:

`finalDelta[player] = (winDelta[player] + kongDelta[player]) * base`

### `winDelta`

For each non-winner A, B, C, calculate that player’s `lostFan`

For discard win:

- `winDelta[discarder] = -1 * (lostFanA + lostFanB + lostFanC)`
- `winDelta[winner] = (lostFanA + lostFanB + lostFanC)`
- `winDelta[others] = 0`

For self-draw win:

- `winDelta[A] = -1 * lostFanA`
- `winDelta[B] = -1 * lostFanB`
- `winDelta[C] = -1 * lostFanC`
- `winDelta[winner] = (lostFanA + lostFanB + lostFanC)`

If there is no winner:

- `winDelta[dealer] = 3`
- `winDelta[each non-dealer] = -1`

#### Basic Win & Single Win

`lostFan = baseFan * applicableDoubles`, where `applicableDoubles` is the product of all applicable `x2` multipliers.

For basic win, `baseFan = 1`, for single win, `baseFan = 2`. Single win replaces Basic win fan; it is not added on top of Basic win.

Applicable doubles include:

- self-draw double, if the winner self-draws
- discarder double, if this loser is the discarder
- dealer double, if the winner is dealer or this loser is dealer
- closed-hand double, if this loser has not opened
- three-closed-losers double, if all three losers are still closed
- kong-replacement-win double, if winner declares kong, draws replacement tile, and wins immediately

#### Seven Pairs

For discard win:

For discarder, `lostFan = 40`; the other two losers: `lostFan = 30`

For self-draw win:

All three losers: `lostFan = 40`

#### All Triplets

For discard win:

For discarder, `lostFan = 30`; the other two losers: `lostFan = 20`

For self-draw win:

All three losers: `lostFan = 30`

Seven pairs and all triplets use fixed `lostFan` values. Dealer, closed-hand, three-closed-losers, self-draw, discarder, and kong-replacement-win doubles do not apply unless explicitly stated.

### `kongDelta`

Kong reward is calculated per kong event, per player, independently from who wins.

Each kong event produces its own payer/payee deltas:

- Direct exposed kong: discarder pays kong declarer.
- Concealed kong: all three other players pay kong declarer.
- Upgraded kong: all three other players pay kong declarer.

Then all kong event deltas are summed into `kongDelta[player]`.

- Direct exposed kong is 2 fan.
- Direct exposed kong of dragon tiles is 4 fan.
- Upgraded kong is 2 fan.
- Upgraded kong of dragon tiles is 4 fan.
- Concealed kong is 4 fan.
- Concealed kong of dragon tiles is 8 fan.

## Examples

Each example should include input and expected output. Add these before coding the matching rule.

### Example 1: Basic win on discard

Input:

- Base: `1`
- Dealer: `N`
- Winner: `S`
- Win method: discard
- Discarder: `W`
- Winning category: Basic win
- Losers: `E`, `W`, `N`
- Open status:
  - `E`: opened
  - `W`: opened
  - `N`: opened
- Kong events: none

Calculation:

- `lostFanE = 1`
- `lostFanW = 1 * 2 = 2` because `W` is the discarder
- `lostFanN = 1 * 2 = 2` because `N` is the dealer
- `winDelta[W] = -1 * (1 + 2 + 2) = -5`
- `winDelta[S] = 5`
- `winDelta[E] = 0`
- `winDelta[N] = 0`
- `kongDelta` is `0` for all players

Expected final delta:

- `E`: `0`
- `S`: `+5`
- `W`: `-5`
- `N`: `0`

### Example 2: Single win by dealer self-draw

Input:

- Base: `1`
- Dealer: `E`
- Winner: `E`
- Win method: self-draw
- Winning category: Single win
- Losers: `S`, `W`, `N`
- Open status:
  - `S`: opened
  - `W`: opened
  - `N`: opened
- Kong events: none

Calculation:

- Single win `baseFan = 2`
- Each loser has self-draw double and dealer double because winner is dealer
- `lostFanS = 2 * 2 * 2 = 8`
- `lostFanW = 2 * 2 * 2 = 8`
- `lostFanN = 2 * 2 * 2 = 8`
- `winDelta[E] = 8 + 8 + 8 = 24`
- `winDelta[S] = -8`
- `winDelta[W] = -8`
- `winDelta[N] = -8`
- `kongDelta` is `0` for all players

Expected final delta:

- `E`: `+24`
- `S`: `-8`
- `W`: `-8`
- `N`: `-8`

### Example 3: Basic self-draw with all three losers closed

Input:

- Base: `1`
- Dealer: `S`
- Winner: `W`
- Win method: self-draw
- Winning category: Basic win
- Losers: `E`, `S`, `N`
- Open status:
  - `E`: closed
  - `S`: closed
  - `N`: closed
- Kong events: none

Calculation:

- Basic win `baseFan = 1`
- All losers have self-draw double, closed-hand double, and three-closed-losers double
- `S` also has dealer double
- `lostFanE = 1 * 2 * 2 * 2 = 8`
- `lostFanS = 1 * 2 * 2 * 2 * 2 = 16`
- `lostFanN = 1 * 2 * 2 * 2 = 8`
- `winDelta[W] = 8 + 16 + 8 = 32`
- `winDelta[E] = -8`
- `winDelta[S] = -16`
- `winDelta[N] = -8`
- `kongDelta` is `0` for all players

Expected final delta:

- `E`: `-8`
- `S`: `-16`
- `W`: `+32`
- `N`: `-8`

### Example 4: Seven pairs on discard

Input:

- Base: `1`
- Dealer: `E`
- Winner: `S`
- Win method: discard
- Discarder: `W`
- Winning category: Seven pairs
- Losers: `E`, `W`, `N`
- Kong events: none

Calculation:

- Seven pairs uses fixed `lostFan`
- `lostFanW = 40` because `W` is the discarder
- `lostFanE = 30`
- `lostFanN = 30`
- Normal doubles do not apply
- `winDelta[W] = -1 * (30 + 40 + 30) = -100`
- `winDelta[S] = 100`
- `winDelta[E] = 0`
- `winDelta[N] = 0`
- `kongDelta` is `0` for all players

Expected final delta:

- `E`: `0`
- `S`: `+100`
- `W`: `-100`
- `N`: `0`

### Example 5: All triplets by self-draw

Input:

- Base: `1`
- Dealer: `N`
- Winner: `E`
- Win method: self-draw
- Winning category: All triplets
- Losers: `S`, `W`, `N`
- Kong events: none

Calculation:

- All triplets uses fixed `lostFan`
- `lostFanS = 30`
- `lostFanW = 30`
- `lostFanN = 30`
- Normal doubles do not apply
- `winDelta[E] = 30 + 30 + 30 = 90`
- `winDelta[S] = -30`
- `winDelta[W] = -30`
- `winDelta[N] = -30`
- `kongDelta` is `0` for all players

Expected final delta:

- `E`: `+90`
- `S`: `-30`
- `W`: `-30`
- `N`: `-30`

### Example 6: Kong rewards only

Input:

- Base: `1`
- Win result: none
- Dealer: `E`
- Kong events:
  - `S` declares a direct exposed kong from `W` using non-dragon tiles
  - `N` declares a concealed kong using dragon tiles

Calculation:

- No winner:
  - `winDelta[E] = +3` because `E` is the dealer
  - `winDelta[S] = -1`
  - `winDelta[W] = -1`
  - `winDelta[N] = -1`
- Direct exposed kong is `2` fan: `W` pays `S`
- Concealed kong of dragon tiles is `8` fan: `E`, `S`, and `W` each pay `N`
- `kongDelta[S] = +2 - 8 = -6`
- `kongDelta[W] = -2 - 8 = -10`
- `kongDelta[E] = -8`
- `kongDelta[N] = +8 + 8 + 8 = +24`

Expected final delta:

- `E`: `+3 - 8 = -5`
- `S`: `-1 - 6 = -7`
- `W`: `-1 - 10 = -11`
- `N`: `-1 + 24 = +23`

### Example 7: Basic win plus kong rewards

Input:

- Base: `1`
- Dealer: `E`
- Winner: `S`
- Win method: discard
- Discarder: `W`
- Winning category: Basic win
- Losers: `E`, `W`, `N`
- Open status:
  - `E`: opened
  - `W`: opened
  - `N`: opened
- Kong events:
  - `W` declares an upgraded kong using non-dragon tiles

Calculation:

- Basic win:
  - `lostFanE = 1 * 2 = 2` because `E` is the dealer
  - `lostFanW = 1 * 2 = 2` because `W` is the discarder
  - `lostFanN = 1`
  - `winDelta[W] = -1 * (2 + 2 + 1) = -5`
  - `winDelta[S] = +5`
  - `winDelta[E] = 0`
  - `winDelta[N] = 0`
- Upgraded kong is `2` fan, paid by all three other players:
  - `kongDelta[W] = +6`
  - `kongDelta[E] = -2`
  - `kongDelta[S] = -2`
  - `kongDelta[N] = -2`

Expected final delta:

- `E`: `-2`
- `S`: `+3`
- `W`: `+1`
- `N`: `-2`
