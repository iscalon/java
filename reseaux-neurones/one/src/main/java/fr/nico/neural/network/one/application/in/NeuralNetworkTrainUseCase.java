package fr.nico.neural.network.one.application.in;

import fr.nico.neural.network.one.application.shared.DataSetProperties;

public interface NeuralNetworkTrainUseCase {

  void trainAndSaveNetworkUsing(DataSetProperties dataSetProperties);
}
