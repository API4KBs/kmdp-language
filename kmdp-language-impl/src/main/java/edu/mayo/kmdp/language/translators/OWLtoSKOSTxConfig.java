package edu.mayo.kmdp.language.translators;

import edu.mayo.kmdp.ConfigProperties;
import edu.mayo.kmdp.Opt;
import edu.mayo.kmdp.Option;
import edu.mayo.kmdp.language.detectors.OWLDetectorConfig;
import edu.mayo.kmdp.language.translators.OWLtoSKOSTxConfig.OWLtoSKOSTxParams;
import java.util.Properties;

public class OWLtoSKOSTxConfig extends ConfigProperties<OWLtoSKOSTxConfig, OWLtoSKOSTxParams> {

  private static final Properties defaults = defaulted( OWLDetectorConfig.DetectorParams.class );

  public OWLtoSKOSTxConfig() {
    super( defaults );
  }

  public OWLtoSKOSTxConfig(Properties defaults) {
    super(defaults);
  }

  @Override
  protected OWLtoSKOSTxConfig.OWLtoSKOSTxParams[] properties() {
    return OWLtoSKOSTxConfig.OWLtoSKOSTxParams.values();
  }


  public enum OWLtoSKOSTxParams implements Option<OWLtoSKOSTxConfig.OWLtoSKOSTxParams> {

    TGT_NAMESPACE( Opt.of( "targetNamespace", "", String.class ) );

    private Opt opt;

    OWLtoSKOSTxParams( Opt opt ) {
      this.opt = opt;
    }

    @Override
    public Opt getOption() {
      return opt;
    }

  }
}
