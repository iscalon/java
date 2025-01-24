package fr.nico.neural.network.one.application.shared;

import lombok.Builder;

@Builder
public record XYGraphProperties(
    String graphicName, int width, int height, String xAxisTitle, String yAxisTitle) {}
