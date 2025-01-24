package fr.nico.neural.network.one.application.out;

import fr.nico.neural.network.one.application.shared.DataSetProperties;
import org.encog.ml.data.MLDataSet;

public interface DataRepository {

  MLDataSet readDataSet(DataSetProperties properties);
}
