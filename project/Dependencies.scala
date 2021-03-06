import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt._


object Dependencies
{

  val shared: Def.Initialize[Seq[ModuleID]] = Def.setting(Seq())

  val compilers: Def.Initialize[Seq[ModuleID]] = Def.setting(shared.value ++ Seq(
    "org.scala-lang" % "scala-compiler" % Versions.scala
  ))

  val preview = Def.setting(shared.value ++  Seq(

    "com.vmunier" %% "play-scalajs-scripts" %  Versions.playScripts,

    "org.scala-lang.modules" %% "scala-async" % "0.9.2",

    "org.denigma" %% "semweb-sesame" % Versions.semWeb,

    "org.denigma" %% "schemas" % Versions.schemas,

    "org.w3" %% "banana-sesame" % Versions.banana excludeAll ExclusionRule(organization = "org.openrdf.sesame"), //sesame bunding to bananardf

    "org.openrdf.sesame" % "sesame-rio-rdfxml" % Versions.sesame,

    "org.openrdf.sesame" % "sesame-rio-turtle" % Versions.sesame,

    "com.github.marklister" %% "product-collections" % Versions.productCollections,

    "com.markatta" %% "scalenium" % Versions.scalenium % "test"  excludeAll ExclusionRule(organization = "org.specs2"),

    "org.specs2" %% "specs2-core" % Versions.specs2 % "test",

    "com.github.cb372" %% "scalacache-lrumap" % Versions.lruMap,

    "com.github.japgolly.scalacss" %% "core" % Versions.scalaCSS

  )++webjars)


  protected lazy val webjars: Seq[ModuleID] = Seq(

    "org.webjars" % "jquery" % Versions.jquery,

    "org.webjars" % "Semantic-UI" % Versions.semanticUI,

    "org.webjars" % "codemirror" % Versions.codemirror,

    "org.webjars" % "ckeditor" % Versions.ckeditor,

    "org.webjars" % "N3.js" % Versions.N3,

    "org.webjars" % "three.js" % Versions.threeJS,

    "org.webjars" % "selectize.js" % Versions.selectize
  )

  val macro_js = Def.setting(shared.value++Seq(
      "org.scala-js" %%% "scalajs-dom" % Versions.dom,

      "com.lihaoyi" %%% "scalatags" % Versions.scalaTags,

      "com.lihaoyi" %%% "scalarx" % Versions.scalaRx
  ))

  val models_js = Def.setting(shared.value++Seq(
      "org.denigma" %%% "semweb" % Versions.semWeb,

      "com.lihaoyi" %%% "scalarx" % Versions.scalaRx
  ))

  val models_jvm = Def.setting(shared.value++Seq(

      "org.denigma" %% "semweb" % Versions.semWeb,

      "com.lihaoyi" %% "scalarx" % Versions.scalaRx

  ))

  val bindingJS: Def.Initialize[Seq[ModuleID]] = Def.setting(shared.value++Seq(

    "org.scala-js" %%% "scalajs-dom" % Versions.dom,

    "org.querki" %%% "jquery-facade" % Versions.jqueryFacade,

    "org.denigma" %%% "codemirror-facade" % Versions.codeMirrorFacade,

    "org.denigma" %%% "selectize-facade" % Versions.selectizeFacade,

    "com.softwaremill.quicklens" %%% "quicklens" % Versions.quicklens,

    "com.lihaoyi" %%% "scalarx" % Versions.scalaRx

  )  )

  val bindingJVM: Def.Initialize[Seq[ModuleID]] = Def.setting(shared.value++Seq(

    "com.softwaremill.quicklens" %% "quicklens" % Versions.quicklens,

    "com.lihaoyi" %% "scalarx" % Versions.scalaRx

  )  )

  val semanticBinding: Def.Initialize[Seq[ModuleID]]  = Def.setting(shared.value++Seq(
    "org.w3" %%% "banana-n3-js" % Versions.banana excludeAll ExclusionRule(organization = "com.github.inthenow")
  ))


  val bindingPlay = Def.setting(shared.value++Seq(

    "com.typesafe.play" %% "play" %  Versions.play, //% "provided",

    "net.ruippeixotog" %% "scala-scraper" % Versions.scraper
  ))

  val ui = Def.setting(shared.value++Seq(
    "com.github.marklister" %%% "product-collections" % Versions.productCollections
  ))



}
