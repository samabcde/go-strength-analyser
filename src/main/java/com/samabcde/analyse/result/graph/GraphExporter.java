package com.samabcde.analyse.result.graph;

import com.samabcde.analyse.sgf.Rank;
import com.samabcde.analyse.statistic.BigDecimalSummaryStatistics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class GraphExporter {
    public void export(Map<Rank, BigDecimalSummaryStatistics> rankToStatistic) {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        rankToStatistic.forEach((rank, statistic) -> {
            dataset.addValue(statistic.getAverage().doubleValue(), rank.code(), Integer.valueOf(rank.level()));
        });

        final XYSeries xySeries = new XYSeries("V1");
        rankToStatistic.forEach((rank, statistic) -> {
            xySeries.add(new XYDataItem(rank.level(), statistic.getAverage()));
        });
        var xyDataset = new XYSeriesCollection();
        xyDataset.addSeries(xySeries);

        JFreeChart xyChart = ChartFactory.createXYLineChart("Average GSS V1"
                , "Rank", "GSS", xyDataset, PlotOrientation.VERTICAL, true, true, false);
        int width = 1440;
        int height = 600;
        xyChart.getXYPlot().getDomainAxis().setAutoRange(false);
        xyChart.getXYPlot().getDomainAxis().setStandardTickUnits(new RankTickUnitSource());//.getRenderer().setSeriesItemLabelGenerator(0, (dataset1, series, item) -> Rank.valueByLevel(dataset1.getX(series, item).intValue()).code());
        File xyChartJpeg = new File("XYChart.jpeg");
        try {
            ChartUtils.saveChartAsJPEG(xyChartJpeg, xyChart, width, height);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}







