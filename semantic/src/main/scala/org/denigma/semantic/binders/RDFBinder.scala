package org.denigma.semantic.binders

import org.denigma.binding.binders.BasicBinding
import org.denigma.binding.views.BindableView
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLElement
import org.denigma.semweb.rdf._
import org.denigma.binding.extensions._
import org.w3.banana.Prefix
import rx.core.Var
import scala.Predef
import scala.collection.immutable.Map
import org.denigma.semweb.rdf.vocabulary
import org.denigma.semweb


object RDFBinder {
  lazy val defaultPrefixes: Predef.Map[String, IRI] = Map(
    "rdf"->IRI(vocabulary.RDF.namespace),
    "rdfs"->IRI(vocabulary.RDFS.namespace),
    "xsd"->IRI(vocabulary.XSD.namespace),
    "owl"->IRI(vocabulary.OWL.namespace),
    "dc"->IRI(vocabulary.DCElements.namespace),
    "dcterms"->IRI(vocabulary.DCTerms.namespace),
    "foaf"->IRI(vocabulary.FOAF.namespace),
    "pl"->IRI(vocabulary.WI.PLATFORM.namespace),
    "wi"->IRI(vocabulary.WI.namespace),
    "de"->IRI("http://denigma.org/resource/"),
    "gero"-> IRI("http://gero.longevityalliance.org/"),
    "se"->semweb.shex.se,
    "rs"->semweb.shex.rs,
    "pubmed"->IRI("http://www.ncbi.nlm.nih.gov/pubmed/")
  )
}

trait PrefixResolver {


  def resolve(property:String,prefixes:Map[String,IRI]):Option[IRI]  = property.indexOf(":") match {
    case -1 =>
      //dom.alert(prefixes.toString())
      prefixes.get(":").orElse(prefixes.get("")).map(p=>p / property)
    case 0 =>
      //dom.alert("0"+prefixes.toString())
      prefixes.get(":").orElse(prefixes.get("")).map(p=>p / property)
    case ind=>
      val key = property.substring(0,ind)

      prefixes.get(key).map(p=>p / property).orElse(Some(IRI(property)))
  }

  protected def stringIsIRI(str:String) = str.contains(":") && !str.contains("^^")


  protected def asPrefix(str:String) = if(str.last==':') str else str+":"



  protected def prefixForIri(iri:String,prefixes:Map[String,IRI]): Option[(String, IRI)] = prefixes.collectFirst{
    case (key,value) if iri.startsWith(value.stringValue)=> (key,value)
  }

  protected def prefixedIRI(iri:String,prefixes:Map[String,IRI]):String =  prefixes.collectFirst{
    case (key,value) if iri.startsWith(value.stringValue)=>
      iri.replace(value.stringValue,asPrefix(key))
  }.getOrElse(iri)

  protected def prefixedIRI(iri:IRI,prefixes:Map[String,IRI]):String = prefixedIRI(iri.stringValue,prefixes)

}

/**
 * View that can do RDFa binding (binding to semantic properties)
 */
class RDFBinder(view:BindableView,val prefixes:Var[Map[String,IRI]] = Var(RDFBinder.defaultPrefixes)) extends
BasicBinding with PrefixResolver
{

  implicit val context = IRI("http://"+dom.location.hostname)

  /**
   * Resolvers IRI from property map
   * @param property
   * @return
   */
  def resolve(property:String):Option[IRI] =  this.resolve(property,prefixes.now)


  /**
   * Returns IRI in a nicely prefixed way
   * @param iri
   * @return
   */
  def prefixedIRIString(iri:IRI): String = prefixes.now.find{ case (key,value)=>iri.stringValue.contains(value)}.map
  {
    //TODO: test if it works and includes ":"
    case (key,value)=> iri.stringValue.replace(value.stringValue,key)
  }.getOrElse(iri.stringValue)

  def nearestRDFBinders(): List[RDFBinder] = view.nearestParentOf{
    case p:BindableView if p.binders.exists(b=>b.isInstanceOf[RDFBinder])=>p.binders.collect{case b:RDFBinder=>b}
  }.getOrElse(List.empty)


  protected def updatePrefixes(el:HTMLElement) = {
    val rps: List[RDFBinder] = this.nearestRDFBinders()
    prefixes.set(rps.foldLeft(Map.empty[String,IRI])((acc,el)=>acc++el.prefixes.now)++this.prefixes.now)
  }

  protected def forBinding(str:String) = str.contains("data") && str.contains("bind")


  /**
   * Returns partial function that extracts vocabulary data (like prefixes) from html
   * @param value
   * @return
   */
  protected def vocabPartial(value: String): PartialFunction[String, Unit] ={

    case "vocab" if value.contains(":") => this.prefixes.set(prefixes.now + (":"-> IRI(value)))
    //dom.alert("VOCAB=>"+prefixes.toString())

    case "prefix" if value.contains(":")=> this.prefixes.set(prefixes.now + (value.substring(0,value.indexOf(":"))-> IRI(value)))
  }


  /**
   * Binds vocabs and prefixes
   * @param el html element to bind to
   * @param key Key
   * @param value Value
   * @param ats attributes
   * @return
   */
  protected def rdfPartial(el: HTMLElement, key: String, value: String, ats:Map[String,String]): PartialFunction[String, Unit] =  this.vocabPartial(value)



  override def bindAttributes(el: HTMLElement, ats: Map[String, String]): Unit = {

    this.updatePrefixes(el)

    ats.foreach { case (key, value) =>
      this.rdfPartial(el, key, value,ats).orElse(otherPartial)(key)
    }

  }

  /**
   * If it has "value" property"
   * @param el
   * @return
   */
  protected def elementHasValue(el:HTMLElement) =  el.tagName.toLowerCase match {
    case "input" | "textarea" | "option" =>true
    case _ =>false
  }


  def setTitle(el:HTMLElement,tlt:String) = {
    if(this.elementHasValue(el)) {
      el.dyn.value = tlt

    } else {
      el.textContent = tlt
    }
  }

  override def id: String = view.id
}
