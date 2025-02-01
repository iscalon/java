package fr.nico.neural.network.one.application.out;

import fr.nico.neural.network.one.application.shared.DataSetProperties;
import org.encog.ml.data.MLDataSet;

public interface DataRepository<N> {

  MLDataSet readDataSet(DataSetProperties properties);

  String saveNetwork(String networkFileName, N network);

  N loadNetwork(String networkFileName);
}
