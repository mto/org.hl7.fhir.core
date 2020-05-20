package org.hl7.fhir.r5.renderers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.hl7.fhir.exceptions.DefinitionException;
import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.r5.model.DomainResource;
import org.hl7.fhir.r5.model.CapabilityStatement;
import org.hl7.fhir.r5.model.CapabilityStatement.CapabilityStatementRestComponent;
import org.hl7.fhir.r5.model.CapabilityStatement.CapabilityStatementRestResourceComponent;
import org.hl7.fhir.r5.model.CapabilityStatement.ResourceInteractionComponent;
import org.hl7.fhir.r5.model.CapabilityStatement.SystemInteractionComponent;
import org.hl7.fhir.r5.model.CapabilityStatement.SystemRestfulInteraction;
import org.hl7.fhir.r5.model.CapabilityStatement.TypeRestfulInteraction;
import org.hl7.fhir.r5.renderers.utils.RenderingContext;
import org.hl7.fhir.r5.renderers.utils.Resolver.ResourceContext;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;

public class CapabilityStatementRenderer extends ResourceRenderer {

  public CapabilityStatementRenderer(RenderingContext context) {
    super(context);
  }

  public CapabilityStatementRenderer(RenderingContext context, ResourceContext rcontext) {
    super(context, rcontext);
  }
  
  public boolean render(XhtmlNode x, DomainResource dr) throws FHIRFormatError, DefinitionException, IOException {
    return render(x, (CapabilityStatement) dr);
  }

  public boolean render(XhtmlNode x, CapabilityStatement conf) throws FHIRFormatError, DefinitionException, IOException {
    x.h2().addText(conf.getName());
    addMarkdown(x, conf.getDescription());
    if (conf.getRest().size() > 0) {
      CapabilityStatementRestComponent rest = conf.getRest().get(0);
      XhtmlNode t = x.table(null);
      addTableRow(t, "Mode", rest.getMode().toString());
      addTableRow(t, "Description", rest.getDocumentation());

      addTableRow(t, "Transaction", showOp(rest, SystemRestfulInteraction.TRANSACTION));
      addTableRow(t, "System History", showOp(rest, SystemRestfulInteraction.HISTORYSYSTEM));
      addTableRow(t, "System Search", showOp(rest, SystemRestfulInteraction.SEARCHSYSTEM));

      boolean hasVRead = false;
      boolean hasPatch = false;
      boolean hasDelete = false;
      boolean hasHistory = false;
      boolean hasUpdates = false;
      for (CapabilityStatementRestResourceComponent r : rest.getResource()) {
        hasVRead = hasVRead || hasOp(r, TypeRestfulInteraction.VREAD);
        hasPatch = hasPatch || hasOp(r, TypeRestfulInteraction.PATCH);
        hasDelete = hasDelete || hasOp(r, TypeRestfulInteraction.DELETE);
        hasHistory = hasHistory || hasOp(r, TypeRestfulInteraction.HISTORYTYPE);
        hasUpdates = hasUpdates || hasOp(r, TypeRestfulInteraction.HISTORYINSTANCE);
      }

      t = x.table(null);
      XhtmlNode tr = t.tr();
      tr.th().b().tx("Resource Type");
      tr.th().b().tx("Profile");
      tr.th().b().attribute("title", "GET a resource (read interaction)").tx("Read");
      if (hasVRead)
        tr.th().b().attribute("title", "GET past versions of resources (vread interaction)").tx("V-Read");
      tr.th().b().attribute("title", "GET all set of resources of the type (search interaction)").tx("Search");
      tr.th().b().attribute("title", "PUT a new resource version (update interaction)").tx("Update");
      if (hasPatch)
        tr.th().b().attribute("title", "PATCH a new resource version (patch interaction)").tx("Patch");
      tr.th().b().attribute("title", "POST a new resource (create interaction)").tx("Create");
      if (hasDelete)
        tr.th().b().attribute("title", "DELETE a resource (delete interaction)").tx("Delete");
      if (hasUpdates)
        tr.th().b().attribute("title", "GET changes to a resource (history interaction on instance)").tx("Updates");
      if (hasHistory)
        tr.th().b().attribute("title", "GET changes for all resources of the type (history interaction on type)").tx("History");

      for (CapabilityStatementRestResourceComponent r : rest.getResource()) {
        tr = t.tr();
        tr.td().addText(r.getType());
        if (r.hasProfile()) {
          tr.td().ah(context.getPrefix()+r.getProfile()).addText(r.getProfile());
        }
        tr.td().addText(showOp(r, TypeRestfulInteraction.READ));
        if (hasVRead)
          tr.td().addText(showOp(r, TypeRestfulInteraction.VREAD));
        tr.td().addText(showOp(r, TypeRestfulInteraction.SEARCHTYPE));
        tr.td().addText(showOp(r, TypeRestfulInteraction.UPDATE));
        if (hasPatch)
          tr.td().addText(showOp(r, TypeRestfulInteraction.PATCH));
        tr.td().addText(showOp(r, TypeRestfulInteraction.CREATE));
        if (hasDelete)
          tr.td().addText(showOp(r, TypeRestfulInteraction.DELETE));
        if (hasUpdates)
          tr.td().addText(showOp(r, TypeRestfulInteraction.HISTORYINSTANCE));
        if (hasHistory)
          tr.td().addText(showOp(r, TypeRestfulInteraction.HISTORYTYPE));
      }
    }

    return true;
  }

  public void describe(XhtmlNode x, CapabilityStatement cs) {
    x.tx(display(cs));
  }

  public String display(CapabilityStatement cs) {
    return cs.present();
  }

  @Override
  public String display(DomainResource r) throws UnsupportedEncodingException, IOException {
    return ((CapabilityStatement) r).present();
  }


  private boolean hasOp(CapabilityStatementRestResourceComponent r, TypeRestfulInteraction on) {
    for (ResourceInteractionComponent op : r.getInteraction()) {
      if (op.getCode() == on)
        return true;
    }
    return false;
  }

  private String showOp(CapabilityStatementRestResourceComponent r, TypeRestfulInteraction on) {
    for (ResourceInteractionComponent op : r.getInteraction()) {
      if (op.getCode() == on)
        return "y";
    }
    return "";
  }

  private String showOp(CapabilityStatementRestComponent r, SystemRestfulInteraction on) {
    for (SystemInteractionComponent op : r.getInteraction()) {
      if (op.getCode() == on)
        return "y";
    }
    return "";
  }


  private void addTableRow(XhtmlNode t, String name, String value) {
    XhtmlNode tr = t.tr();
    tr.td().addText(name);
    tr.td().addText(value);
  }

}