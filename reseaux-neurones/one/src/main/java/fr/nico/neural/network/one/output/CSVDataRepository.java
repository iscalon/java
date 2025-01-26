package fr.nico.neural.network.one.output;

import static fr.nico.neural.network.one.data.normalize.CsvXYFileNormalizer.getFile;

import fr.nico.neural.network.one.application.out.DataRepository;
import fr.nico.neural.network.one.application.shared.DataSetProperties;
import java.io.File;
import java.nio.file.Files;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.buffer.MemoryDataLoader;
import org.encog.ml.data.buffer.codec.CSVDataCODEC;
import org.encog.ml.data.buffer.codec.DataSetCODEC;
import org.encog.neural.networks.BasicNetwork;
import org.encog.persist.EncogDirectoryPersistence;
import org.encog.util.csv.CSVFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class CSVDataRepository implements DataRepository<BasicNetwork> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CSVDataRepository.class);

  @Override
  public MLDataSet readDataSet(DataSetProperties properties) {
    LOGGER.info("Chargement du DataSet à entrainer : {}", properties);
    try {
      DataSetCODEC codec =
          new CSVDataCODEC(
              getFile(properties.fileName()),
              CSVFormat.ENGLISH,
              properties.considerHeader(),
              properties.numberOfInput(),
              properties.numberOfOutput(),
              properties.significance());
      MemoryDataLoader loader = new MemoryDataLoader(codec);
      return loader.external2Memory();
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Problème à la récupération du DataSet de données servant à entrainer le réseau, propriétés : "
              + properties,
          e);
    }
  }

  @Override
  public void saveNetwork(String networkFileName, BasicNetwork network) {
    try {
      File outputFile = Files.createTempFile(networkFileName, ".network").toFile();
      EncogDirectoryPersistence.saveObject(outputFile, network);
      LOGGER.info("Réseau sauvegardé dans le fichier : {}", outputFile);
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Problème lors de la sauvegarde du réseau dans le fichier : " + networkFileName, e);
    }
  }

  @Override
  public BasicNetwork loadNetwork(String networkFileName) {
    try {
      return (BasicNetwork) EncogDirectoryPersistence.loadObject(getFile(networkFileName));
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Problème lors du chargement du réseau via le fichier : " + networkFileName, e);
    }
  }
}
