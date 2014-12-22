package controllers

import java.net.URLClassLoader

import org.apache.spark._
import org.apache.spark.SparkContext._

import play.api._
import play.api.mvc._

import scala.annotation.tailrec

import scalax.file.Path
import scalax.file.ImplicitConversions.string2path

object Application extends Controller {

  @tailrec
  private def addClassPathJars(sparkContext: SparkContext, classLoader: ClassLoader): Unit = {
    classLoader match {
      case urlClassLoader: URLClassLoader => {
        urlClassLoader.getURLs.foreach { classPathUrl =>
          if (classPathUrl.toExternalForm.endsWith(".jar"))
            sparkContext.addJar(classPathUrl.toExternalForm)
        }
      }
      case _ =>
    }
    if (classLoader.getParent != null) {
      addClassPathJars(sparkContext, classLoader.getParent)
    }
  }

  private def addJars(sparkContext: SparkContext, path: Path, libs: Array[String]): Unit = {
    val jars = libs.flatMap(l => path.parent.map(p => (p / l).path))
    jars.foreach(sparkContext.addJar(_))
  }

  def index = Action {
    val libs = Array(
      "spark_cluster_sample_2.10-1.0-SNAPSHOT.jar"
    )
    val conf = new SparkConf()
      .setMaster("spark://Tomoya-Igarashis-MacBook-Air.local:7077")
      .setAppName("SparkClusterSample")
      .set("spark.logConf", "true")
    val sc = new SparkContext(conf)
    addClassPathJars(sc, getClass.getClassLoader)
    addJars(sc, getClass.getProtectionDomain.getCodeSource.getLocation.getPath, libs)
    val rdd = sc.parallelize(List(1, 2, 3, 4))
    val result = rdd.aggregate((0, 0))(
      (x, y) => (x._1 + y, x._2 + 1),
      (x, y) => (x._1 + y._1, x._2 + y._2)
    )
    Logger.debug("result = %s".format(result.toString))
    sc.stop()
    Ok(views.html.index("Your new application is ready."))
  }

}
