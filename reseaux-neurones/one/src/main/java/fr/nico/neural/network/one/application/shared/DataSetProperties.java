package fr.nico.neural.network.one.application.shared;

import lombok.Builder;

@Builder
public record DataSetProperties(
    String fileName,
    int numberOfInput,
    int numberOfOutput,
    boolean considerHeader,
    boolean significance) {}
