This module contains the core gameplay logic for `Zeldalike`.

It will make heavy use of the library components provided by `game2d`.

Note that this code is pure, platform-agnostic logic. To actually run the game
for a specific target, e.g. desktop, you should find the appropriate module,
e.g. `zeldalike-desktop`, which sets up all the platform-specific plumbing.
