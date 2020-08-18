package edu.mayo.kmdp.language.translators.surrogate.v1;

import org.omg.spec.api4kp.taxonomy.dependencyreltype.DependencyType;
import org.omg.spec.api4kp.taxonomy.dependencyreltype.DependencyTypeSeries;
import org.omg.spec.api4kp.taxonomy.derivationreltype.DerivationType;
import org.omg.spec.api4kp.taxonomy.derivationreltype.DerivationTypeSeries;
import org.omg.spec.api4kp.taxonomy.relatedversiontype.RelatedVersionType;
import org.omg.spec.api4kp.taxonomy.relatedversiontype.RelatedVersionTypeSeries;
import org.omg.spec.api4kp.taxonomy.structuralreltype.StructuralPartType;
import org.omg.spec.api4kp.taxonomy.structuralreltype.StructuralPartTypeSeries;
import org.omg.spec.api4kp.taxonomy.summaryreltype.SummarizationType;
import org.omg.spec.api4kp.taxonomy.summaryreltype.SummarizationTypeSeries;
import org.omg.spec.api4kp.taxonomy.variantreltype.VariantType;
import org.omg.spec.api4kp.taxonomy.variantreltype.VariantTypeSeries;

public class TermMapper {

  public static StructuralPartType mapComponentRels(
      edu.mayo.ontology.taxonomies.kao.rel.structuralreltype.StructuralPartType comp) {
    if (comp == null) {
      return null;
    }
    switch (comp.asEnum()) {
      case Structurally_Organized_By:
        return StructuralPartTypeSeries.Has_Structuring_Component;
      case The_Relationship_Between_A_Composite_And_Its_Parts:
        return StructuralPartTypeSeries.Has_Structural_Component;
      case Has_Part:
      case Has_Proper_Part:
        return StructuralPartTypeSeries.Has_Proper_Part;

      case Defines_In_Terms_Of:
      case Captures:
      case Defines:
      case Uses_Concept:
      case Embeds_Language:
      case Leverages:
      default:
        throw new UnsupportedOperationException(
            "Cannot map legacy structural relationship " + comp.getName());
    }
  }

  public static DerivationType mapDerivative(
      edu.mayo.ontology.taxonomies.kao.rel.derivationreltype.DerivationType der) {
    switch (der.asEnum()) {
      case Derived_From:
        return DerivationTypeSeries.Is_Derived_From;
      case Flattening_Of:
        return DerivationTypeSeries.Is_Flattening_Of;
      case Adaptation_Of:
        return DerivationTypeSeries.Is_Adaptation_Of;
      case Transcreation_Of:
        return DerivationTypeSeries.Is_Transcreation_Of;
      case Is_Later_Revision_Of:
        return DerivationTypeSeries.Is_Revision_Of;

      case Has_Original:
      case Imitation_Of:
      case Digest_Of:
      case Inspired_By:
      case Synopsis_Of:
      case Paraphrase_Of:
      case Compression_Of:
      case Translation_Of:
      case Abdridgement_Of:
      case Rearrangement_Of:
      case Summarization_Of:
      case Transcription_Of:
      case Transwordation_Of:
      case Transliteration_Of:
      case Transcodification_Of:
      case Linguistic_Adaptation_Of:
      case Is_Later_Versioned_Revision_Of:
      case Is_Immediate_Next_Versioned_Revision_Of:
      default:
        throw new UnsupportedOperationException(
            "Cannot map legacy derivation relationship " + der.getName());

    }
  }

  public static DependencyType mapDependency(
      edu.mayo.ontology.taxonomies.kao.rel.dependencyreltype.DependencyType dep) {
    switch (dep.asEnum()) {
      case Imports:
        return DependencyTypeSeries.Imports;
      case Includes:
        return DependencyTypeSeries.Includes_By_Reference;
      case Depends_On:
        return DependencyTypeSeries.Depends_On;
      case Complies_With:
        return DependencyTypeSeries.Conforms_With;
      case Effectuates:
        return DependencyTypeSeries.Effectuates;
      case Complements:
      case Supplements:
      default:
        throw new UnsupportedOperationException(
            "Cannot map legacy dependency relationship " + dep.getName());
    }
  }

  public static VariantType mapVariant(
      edu.mayo.ontology.taxonomies.kao.rel.variantreltype.VariantType var) {
    switch (var.asEnum()) {
      case Transcodification_Of:
        return VariantTypeSeries.Is_Transcodification_Of;
      case Transliteration_Of:
        return VariantTypeSeries.Is_Transliteration_Of;
      case Transcription_Of:
        return VariantTypeSeries.Is_Transcription_Of;
      case Rearrangement_Of:
        return VariantTypeSeries.Is_Rearrangement_Of;
      case Translation_Of:
        return VariantTypeSeries.Is_Translation_Of;
      case Alternative_Representation_Of:
        return VariantTypeSeries.Is_Isomorphic_Variant_Of;
      case Linguistic_Adaptation_Of:
      case Compression_Of:
      case Transcreation_Of:
      case Transwordation_Of:
      case Adaptation_Of:
      default:
        throw new UnsupportedOperationException(
            "Cannot map legacy variance relationship " + var.getName());
    }
  }

  public static RelatedVersionType mapRelatedVersion(
      edu.mayo.ontology.taxonomies.kao.rel.relatedversiontype.RelatedVersionType ver) {
    switch (ver.asEnum()) {
      case Has_Next_Version:
        return RelatedVersionTypeSeries.Has_Next_Version;
      case Has_Prior_Version:
        return RelatedVersionTypeSeries.Has_Predecessor_Version;
      case Has_Previous_Version:
        return RelatedVersionTypeSeries.Has_Previous_Version;
      case Has_Successor_Version:
        return RelatedVersionTypeSeries.Has_Successor_Version;
      case Is_Immediate_Next_Versioned_Revision_Of:
      case Is_Later_Versioned_Revision_Of:
      case Has_Original:
      case Has_Latest:
      case Has_Later_Versioned_Revision:
      case Has_Next_Immediate_Versioned_Revision:
      default:
        throw new UnsupportedOperationException(
            "Cannot map legacy version relationship " + ver.getName());
    }
  }

  public static SummarizationType mapSummarization(
      edu.mayo.ontology.taxonomies.kao.rel.summaryreltype.SummarizationType sum) {
    switch (sum.asEnum()) {
      case Compression_Of:
        return SummarizationTypeSeries.Is_Compression_Of;
      case Summarization_Of:
        return SummarizationTypeSeries.Summarizes;
      case Abdridgement_Of:
        return SummarizationTypeSeries.Abridges;
      case Synopsis_Of:
        return SummarizationTypeSeries.Compendiates;
      case Digest_Of:
        return SummarizationTypeSeries.Is_Digest_Of;
      case Compact_Representation_Of:
        return SummarizationTypeSeries.Abbreviates;
      default:
        throw new UnsupportedOperationException(
            "Cannot map legacy summary relationship " + sum.getName());
    }
  }
}
