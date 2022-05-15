# go-strength-analyser

To provide an absolute measurement of go strength, compare
to [Elo Rating System](https://en.wikipedia.org/wiki/Elo_rating_system.)
, which is a relative measurement on the skill level

## How

Use AI(currently katago) to analyse the game, based on the metrics(winrate, scoreLead), we calculate the GSS(Go Strength
Score)
by defined formula.

### Formula (V1)

Basic idea is to measure where the move is located between best move and worst move on the board. So assume:

- best move = best move from AI
- worst move = pass

### Move Score
A linear model will be adopted, if the move is best the score will be 1. If the move is worst, the score
will be -1. If the move is in middle of best and worst, the score will be 0.

2 metrics is used to calculate the score _winrate_ and _scorelead_
So we have 2 scores for each move, winrate score and score lead score.

### Game Score
Black player and White player will have their own Game Score, using the move score

game winrate score = sum(winrate score * weight) / sum(weight)
where weight = best move winrate - worst move winrate
game score lead score = sum(score lead score * weight) / sum(weight)
where weight = best move score lead - worst move score lead

game score = avg(game winrate score, game score lead score) * 10000

### Result
![img.png](formula/result/image/v1.png)

| Rank | Avg      | Min      | Max      | Std     | Count |
|------|----------|----------|----------|---------|-------|
| 35K  | -222.76  | -685.6   | 240.07   | 462.84  | 2     |
| 34K  | -1768.72 | -1768.72 | -1768.72 | 0       | 1     |
| 33K  | -147.44  | -147.44  | -147.44  | 0       | 1     |
| 32K  | 2289.61  | -389.43  | 4796.66  | 2439.26 | 4     |
| 30K  | 910.84   | 910.84   | 910.84   | 0       | 1     |
| 28K  | 3018.93  | 922.71   | 5403.8   | 1608.87 | 4     |
| 27K  | 1851.07  | 723.67   | 3028.85  | 941.76  | 3     |
| 26K  | 485.08   | 485.08   | 485.08   | 0       | 1     |
| 25K  | 2178.92  | 880.55   | 3477.28  | 1298.37 | 2     |
| 24K  | 1817.76  | -1245.62 | 6545.68  | 2593.77 | 5     |
| 23K  | 3415.34  | 1346.74  | 5748.25  | 1806.6  | 3     |
| 22K  | 1623.86  | 452.63   | 3159.14  | 1063.01 | 5     |
| 21K  | 2912.54  | 888.68   | 6385.57  | 1747.96 | 10    |
| 20K  | 2233.39  | -2337.32 | 5649.41  | 2344.54 | 10    |
| 19K  | 1619.02  | -519.4   | 3957.81  | 1594.63 | 4     |
| 18K  | 3830.9   | 1842.72  | 5775.39  | 1158.22 | 11    |
| 17K  | 3686.36  | 138.62   | 6087.89  | 2080.74 | 7     |
| 16K  | 1231.58  | 842.87   | 1620.29  | 388.71  | 2     |
| 15K  | 2889.84  | 2163.86  | 4262.8   | 971.36  | 3     |
| 14K  | 4155.51  | 2377.76  | 6173.55  | 1355.16 | 8     |
| 13K  | 4765.49  | 3853.19  | 5677.79  | 912.3   | 2     |
| 12K  | 5302.7   | 3471.15  | 8292.55  | 1618.29 | 6     |
| 11K  | 4967.74  | 4499.09  | 5650.55  | 493.87  | 3     |
| 10K  | 1489.56  | -2872.43 | 4043.1   | 3099.36 | 3     |
| 9K   | 4914.95  | 3969.89  | 6080.05  | 708.04  | 5     |
| 8K   | 4242.2   | 618.76   | 6929.53  | 1978.05 | 8     |
| 7K   | 6012.87  | 5335.27  | 6690.46  | 677.6   | 2     |
| 6K   | 5377.91  | 3968.77  | 6660.28  | 1129.03 | 5     |
| 5K   | 6431.94  | 3518.18  | 7468.91  | 1398.73 | 6     |
| 4K   | 5541.65  | 3369.3   | 7478.32  | 1223.56 | 10    |
| 3K   | 7023.35  | 7023.35  | 7023.35  | 0       | 1     |
| 2K   | 7183.73  | 7183.73  | 7183.73  | 0       | 1     |
| 1K   | 8422.96  | 8422.96  | 8422.96  | 0       | 1     |
| 2D   | 7947.87  | 7408.05  | 8580.42  | 443.46  | 5     |
| 3D   | 8294.62  | 6963.1   | 9072.75  | 945.99  | 3     |
| 4D   | 9004.41  | 8300.06  | 9704.07  | 573.19  | 3     |
| 5D   | 9225.95  | 8966.49  | 9601.42  | 271.88  | 3     |
| 6D   | 8788.81  | 7678.33  | 9475.95  | 634.8   | 5     |
| 7D   | 9832.28  | 9832.28  | 9832.28  | 0       | 1     |
| 9P   | 9102.16  | 8187.41  | 9757.6   | 403.39  | 19    |
| NR   | 9578.71  | 9578.71  | 9578.71  | 0       | 1     |