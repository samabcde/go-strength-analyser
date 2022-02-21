# go-strength-analyser
To use AI(currently katago) to analyse go strength of a game
Define Gss(Go Strength Score) as:  
(Average of Winrate Score + Average of Point Score) / 2

## Worst Move
Assume pass as the worst move of a game.

## Winrate Score
Winrate Score of a move:

(Current Move Winrate Difference - Worst Move Winrate Difference) / Worst Move Winrate Difference

## Point Score
Point Score of a move:

(Current Move Point Difference - Worst Move Point Difference) / Worst Move Point Difference
