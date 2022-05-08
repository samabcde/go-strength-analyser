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
| 32K  | 2375.61  | 103.75   | 4647.48  | 2271.86 | 2     |
| 30K  | 910.84   | 910.84   | 910.84   | 0       | 1     |
| 28K  | 2068.39  | 922.71   | 3214.08  | 1145.69 | 2     |
| 27K  | 3028.85  | 3028.85  | 3028.85  | 0       | 1     |
| 25K  | 3477.28  | 3477.28  | 3477.28  | 0       | 1     |
| 24K  | 1755.19  | -1245.62 | 6545.68  | 2896.55 | 4     |
| 23K  | 3415.34  | 1346.74  | 5748.25  | 1806.6  | 3     |
| 22K  | 1916.67  | 598.58   | 3159.14  | 991.87  | 4     |
| 21K  | 3137.41  | 964.82   | 6385.57  | 1699.75 | 9     |
| 20K  | 1853.83  | -2337.32 | 4606.73  | 2160.32 | 9     |
| 19K  | 383.24   | -519.4   | 1285.88  | 902.64  | 2     |
| 18K  | 3749.05  | 1842.72  | 5445.36  | 1295.78 | 4     |
| 17K  | 4439.21  | 2080.63  | 5814.93  | 1675.46 | 3     |
| 16K  | 1231.58  | 842.87   | 1620.29  | 388.71  | 2     |
| 15K  | 2203.36  | 2163.86  | 2242.87  | 39.51   | 2     |
| 14K  | 4366.94  | 4366.94  | 4366.94  | 0       | 1     |
| 13K  | 5677.79  | 5677.79  | 5677.79  | 0       | 1     |
| 12K  | 6002.91  | 3713.28  | 8292.55  | 2289.63 | 2     |
| 11K  | 4499.09  | 4499.09  | 4499.09  | 0       | 1     |
| 10K  | 1489.56  | -2872.43 | 4043.1   | 3099.36 | 3     |
| 9K   | 4623.68  | 3969.89  | 5239.06  | 449.95  | 4     |
| 8K   | 1401.83  | 618.76   | 2184.9   | 783.07  | 2     |
| 7K   | 6012.87  | 5335.27  | 6690.46  | 677.6   | 2     |
| 6K   | 5650.53  | 3968.77  | 6660.28  | 1197.15 | 3     |
| 5K   | 5942.3   | 3518.18  | 7468.91  | 1487.98 | 4     |
| 4K   | 4578.33  | 3369.3   | 5153.64  | 708.05  | 4     |
| 2D   | 7947.87  | 7408.05  | 8580.42  | 443.46  | 5     |
| 3D   | 8294.62  | 6963.1   | 9072.75  | 945.99  | 3     |
| 4D   | 9004.41  | 8300.06  | 9704.07  | 573.19  | 3     |
| 5D   | 9225.95  | 8966.49  | 9601.42  | 271.88  | 3     |
| 6D   | 8788.81  | 7678.33  | 9475.95  | 634.8   | 5     |
| 7D   | 9832.28  | 9832.28  | 9832.28  | 0       | 1     |
| 9P   | 9102.16  | 8187.41  | 9757.6   | 403.39  | 19    |
| NR   | 9578.71  | 9578.71  | 9578.71  | 0       | 1     |